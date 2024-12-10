package Hibernate;

import Hibernate.entities.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import res.common.*;

import java.util.List;

public class Queries {
    public static String logIn(Session session, String login, String password) {
        try {
            // Проверяем пользователя с указанным логином и паролем
            User user = session.createQuery("FROM User WHERE login = :login AND password = :password", User.class)
                    .setParameter("login", login)
                    .setParameter("password", password)
                    .uniqueResult();

            if (user != null) {
                // Пользователь найден, возвращаем статус администратора
                return String.valueOf(user.getIsAdmin());
            } else {
                // Проверяем, существует ли пользователь с таким логином
                User existingUser = session.createQuery("FROM User WHERE login = :login", User.class)
                        .setParameter("login", login)
                        .uniqueResult();

                if (existingUser == null) {
                    // Пользователя с таким логином нет
                    return "bad_login";
                } else {
                    // Пользователь есть, но пароль неверный
                    return "bad_password";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "err";
        }
    }

    public static Client getClientByLogin(Session session, String login) {
        try {
            // Получаем пользователя по логину
            User user = session.createQuery("FROM User WHERE login = :login", User.class)
                    .setParameter("login", login)
                    .uniqueResult();

            if (user != null) {
                Client client = Factory.makeClient();
                client.setLogin(user.getLogin());
                client.setPassword(user.getPassword());
                client.setName(user.getName());
                client.setAdmin(user.getIsAdmin());
                return client;
            } else {
                throw new IllegalArgumentException("User with login " + login + " not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving client by login", e);
        }
    }

    private static boolean isLoginInUsers(Session session, String login) {
        String query = "SELECT COUNT(u) FROM User u WHERE u.login = :login";
        Long count = session.createQuery(query, Long.class)
                .setParameter("login", login)
                .uniqueResult();
        return count != null && count > 0;
    }
    private static boolean isLoginOnVerify(Session session, String login) {
        String query = "SELECT COUNT(o) FROM OnVerify o WHERE o.login = :login";
        Long count = session.createQuery(query, Long.class)
                .setParameter("login", login)
                .uniqueResult();
        return count != null && count > 0;
    }
    public static boolean isLoginAvailable(Session session, String login) {
        return !isLoginInUsers(session, login) && !isLoginOnVerify(session, login);
    }

    public static void addNewAccountReg(Session session, String login, String password) {
        OnVerify newAccount = new OnVerify();
        newAccount.setLogin(login);
        newAccount.setPassword(password);

        session.persist(newAccount);
    }

    public static void changePassword(Session session, Client client) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Client existingClient = session.get(Client.class, client.getLogin());
            if (existingClient != null) {
                existingClient.setPassword(client.getPassword());
                session.update(existingClient);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static WorkerArrayList getWorkers(Session session) {
        try {
            // Получаем список DBWorker из базы данных
            List<DBWorker> dbWorkers = session.createQuery("FROM DBWorker", DBWorker.class).list();

            // Преобразуем список DBWorker в WorkerArrayList
            WorkerArrayList workerList = new WorkerArrayList();
            for (DBWorker dbWorker : dbWorkers) {
                res.common.Worker commonWorker = new res.common.Worker(); // Создаём объект Worker из res.common
                commonWorker.setName(dbWorker.getName());
                commonWorker.setAge(dbWorker.getAge());
                commonWorker.setLogin(dbWorker.getLogin());
                commonWorker.setPost(dbWorker.getPost());
                workerList.getList().add(commonWorker);
            }
            return workerList;
        } catch (Exception e) {
            e.printStackTrace();
            return new WorkerArrayList(); // Возвращаем пустой список в случае ошибки
        }
    }

    public static OperationArrayList seeOperationsAtProducts(Session session) {
        try {
            // Выполняем HQL-запрос для операций с целевым типом "Product"
            List<DBOperation> dbOperations = session.createQuery(
                            "FROM DBOperation WHERE targetType = :targetType", DBOperation.class)
                    .setParameter("targetType", "Product")
                    .list();

            // Преобразуем DBOperation в Operation
            OperationArrayList list = new OperationArrayList();
            for (DBOperation dbOperation : dbOperations) {
                Operation operation = Factory.makeOperation();
                operation.setName(dbOperation.getAuthorName());
                operation.setOperationType(dbOperation.getOperationType());
                operation.setTarget(dbOperation.getTarget());
                operation.setInfo(dbOperation.getInformation());
                list.getList().add(operation);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return new OperationArrayList();
        }
    }

    public static ProductArrayList getProducts(Session session) {
        try {
            // Получаем список DBProduct через HQL
            List<DBProduct> dbProducts = session.createQuery("FROM DBProduct", DBProduct.class).list();

            // Преобразуем DBProduct в Product
            ProductArrayList list = new ProductArrayList();
            for (DBProduct dbProduct : dbProducts) {
                Product product = Factory.makeProduct();
                product.setId(dbProduct.getId());
                product.setProdType(dbProduct.getProdType());
                product.setName(dbProduct.getName());
                product.setManufacturer(dbProduct.getManufacturer());
                product.setAmount(dbProduct.getAmount());
                product.setPrice(dbProduct.getPrice());
                list.getList().add(product);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return new ProductArrayList();
        }
    }

    public static void addProduct(Session session, Client client, Product product) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
    
            // Создаём и сохраняем новый продукт
            DBProduct dbProduct = new DBProduct();
            dbProduct.setProdType(product.getProdType());
            dbProduct.setName(product.getName());
            dbProduct.setManufacturer(product.getManufacturer());
            dbProduct.setAmount(product.getAmount());
            dbProduct.setPrice(product.getPrice());
            session.save(dbProduct);
    
            // Создаём и сохраняем операцию
            DBOperation dbOperation = new DBOperation();
            dbOperation.setAuthorLogin(client.getLogin());
            dbOperation.setAuthorName(client.getName());
            dbOperation.setOperationType("Add");
            dbOperation.setTarget(product.getName());
            dbOperation.setTargetType("Product");
            session.save(dbOperation);
    
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static void removeProduct(Session session, Client client, Product product) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
    
            // Удаляем продукт
            DBProduct dbProduct = session.get(DBProduct.class, product.getId());
            if (dbProduct != null) {
                session.delete(dbProduct);
    
                // Создаём и сохраняем операцию удаления
                DBOperation dbOperation = new DBOperation();
                dbOperation.setAuthorLogin(client.getLogin());
                dbOperation.setAuthorName(client.getName());
                dbOperation.setOperationType("Delete");
                dbOperation.setTarget(product.getName());
                dbOperation.setTargetType("Product");
                session.save(dbOperation);
            }
    
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static void editProduct(Session session, Client client, TransferCode code, Product oldProduct, Product newProduct) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
    
            // Получаем продукт из базы данных по ID
            DBProduct dbProduct = session.get(DBProduct.class, oldProduct.getId());
            if (dbProduct == null) {
                System.out.println("Product not found.");
                return;
            }
    
            // Обновляем данные продукта в зависимости от кода изменения
            String whatChanged = "";
            switch (code.getCode()) {
                case "prod_type" -> {
                    whatChanged = "prod_type: " + oldProduct.getProdType() + " -> " + newProduct.getProdType();
                    dbProduct.setProdType(newProduct.getProdType());
                }
                case "name" -> {
                    whatChanged = "name: " + oldProduct.getName() + " -> " + newProduct.getName();
                    dbProduct.setName(newProduct.getName());
                }
                case "manufacturer" -> {
                    whatChanged = "manufacturer: " + oldProduct.getManufacturer() + " -> " + newProduct.getManufacturer();
                    dbProduct.setManufacturer(newProduct.getManufacturer());
                }
                case "amount" -> {
                    whatChanged = "amount: " + oldProduct.getAmount() + " -> " + newProduct.getAmount();
                    dbProduct.setAmount(newProduct.getAmount());
                }
                case "price" -> {
                    whatChanged = "price: " + oldProduct.getPrice() + " -> " + newProduct.getPrice();
                    dbProduct.setPrice(newProduct.getPrice());
                }
                default -> {
                    System.out.println("Unknown change code.");
                    return;
                }
            }
    
            // Сохраняем изменения в базе данных
            session.update(dbProduct);
    
            // Добавляем запись в таблицу операций
            DBOperation dbOperation = new DBOperation();
            dbOperation.setAuthorLogin(client.getLogin());
            dbOperation.setAuthorName(client.getName());
            dbOperation.setOperationType("Edit");
            dbOperation.setTarget(oldProduct.getName());
            dbOperation.setInformation(whatChanged);
            dbOperation.setTargetType("Product");
            session.save(dbOperation);
    
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static ProductTypeArrayList getTypes(Session session) {
        try {
            // Выполняем HQL-запрос для получения всех типов продуктов
            List<DBProductType> dbProductTypes = session.createQuery("FROM DBProductType", DBProductType.class).list();
    
            // Преобразуем DBProductType в ProductType
            ProductTypeArrayList list = new ProductTypeArrayList();
            for (DBProductType dbProductType : dbProductTypes) {
                ProductType type = new ProductType();
                type.setProdType(dbProductType.getProdType());
                type.setDescription(dbProductType.getDescription());
                list.getList().add(type);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return new ProductTypeArrayList(); // Возвращаем пустой список в случае ошибки
        }
    }

    public static void addType(Session session, ProductType type) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
    
            // Создаём объект сущности DBProductType
            DBProductType dbProductType = new DBProductType();
            dbProductType.setProdType(type.getProdType());
            dbProductType.setDescription(type.getDescription());
    
            // Сохраняем объект в базе данных
            session.save(dbProductType);
    
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback(); // Откат транзакции в случае ошибки
            e.printStackTrace();
        }
    }

    public static void addWorker(Session session, Client client, Worker worker) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
    
            // Добавление работника
            DBWorker dbWorker = new DBWorker();
            dbWorker.setName(worker.getName());
            dbWorker.setAge(worker.getAge());
            dbWorker.setPost(worker.getPost());
            session.save(dbWorker);
    
            // Добавление операции
            DBOperation dbOperation = new DBOperation();
            dbOperation.setAuthorLogin(client.getLogin());
            dbOperation.setAuthorName(client.getName());
            dbOperation.setTargetType("Worker");
            dbOperation.setTarget(worker.getName());
            dbOperation.setOperationType("Add");
            session.save(dbOperation);
    
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static void removeWorker(Session session, Client client, Worker worker) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
    
            // Удаление работника
            Query deleteWorkerQuery = session.createQuery("DELETE FROM DBWorker WHERE name = :name");
            deleteWorkerQuery.setParameter("name", worker.getName());
            int affectedRows = deleteWorkerQuery.executeUpdate();
    
            // Если работник найден и удалён, добавляем запись об операции
            if (affectedRows > 0) {
                DBOperation dbOperation = new DBOperation();
                dbOperation.setAuthorLogin(client.getLogin());
                dbOperation.setAuthorName(client.getName());
                dbOperation.setTargetType("Worker");
                dbOperation.setTarget(worker.getName());
                dbOperation.setOperationType("Delete");
                session.save(dbOperation);
            }
    
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static void editWorker(Session session, Client client, TransferCode code, Worker oldWorker, Worker newWorker) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
    
            // Ищем работника в базе данных по имени
            Query<DBWorker> query = session.createQuery("FROM DBWorker WHERE name = :name", DBWorker.class);
            query.setParameter("name", oldWorker.getName());
            DBWorker dbWorker = query.uniqueResult();
    
            if (dbWorker == null) {
                System.out.println("Worker not found!");
                return;
            }
    
            String whatChanged = "";
            switch (code.getCode()) {
                case "login":
                    dbWorker.setLogin(newWorker.getLogin());
                    whatChanged = code.getCode() + ": " + oldWorker.getLogin() + " -> " + newWorker.getLogin();
                    break;
                case "post":
                    dbWorker.setPost(newWorker.getPost());
                    whatChanged = code.getCode() + ": " + oldWorker.getPost() + " -> " + newWorker.getPost();
                    break;
                default:
                    System.out.println("No valid field to update!");
                    return;
            }
    
            // Сохраняем изменения в базе
            session.update(dbWorker);
    
            // Добавляем операцию в базу данных
            DBOperation dbOperation = new DBOperation();
            dbOperation.setAuthorLogin(client.getLogin());
            dbOperation.setAuthorName(client.getName());
            dbOperation.setOperationType("Edit");
            dbOperation.setTarget(oldWorker.getName());
            dbOperation.setInformation(whatChanged);
            session.save(dbOperation);
    
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    public static ClientArrayList getClients(Session session) {
        ClientArrayList list = new ClientArrayList();
    
        try {
            // Получаем всех клиентов из таблицы "users"
            List<DBClient> dbClients = session.createQuery("FROM DBClient", DBClient.class).list();
    
            // Конвертируем DBClient в Client и добавляем в список
            for (DBClient dbClient : dbClients) {
                Client client = Factory.makeClient();
                client.setLogin(dbClient.getLogin());
                client.setPassword(dbClient.getPassword());
                client.setName(dbClient.getName());
                client.setAdmin(dbClient.getIsAdmin());
                list.getList().add(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return list;
    }

    public static ClientArrayList getUnverified(Session session) {
        ClientArrayList list = new ClientArrayList();
    
        try {
            // Получаем всех клиентов из таблицы "on_verify"
            List<DBOnVerify> dbUnverifiedClients = session.createQuery("FROM DBOnVerify", DBOnVerify.class).list();
    
            // Конвертируем DBOnVerify в Client и добавляем в список
            for (DBOnVerify dbOnVerify : dbUnverifiedClients) {
                Client client = Factory.makeClient();
                client.setLogin(dbOnVerify.getLogin());
                list.getList().add(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return list;
    }

    public static void addClient(Session session, Client client, Client user) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
    
            // Получаем пароль из таблицы on_verify
            Query<String> query = session.createQuery("SELECT password FROM DBOnVerify WHERE login = :login", String.class);
            query.setParameter("login", user.getLogin());
            String password = query.uniqueResult();
    
            if (password == null) {
                System.out.println("User not found in verification table.");
                return;
            }
    
            // Устанавливаем параметры нового пользователя
            user.setPassword(password);
            user.setAdmin(0);
    
            // Добавляем пользователя в таблицу users
            DBClient newClient = new DBClient();
            newClient.setLogin(user.getLogin());
            newClient.setPassword(user.getPassword());
            newClient.setName(user.getName());
            newClient.setIsAdmin(user.getAdmin());
            session.save(newClient);
    
            // Удаляем пользователя из on_verify
            Query deleteQuery = session.createQuery("DELETE FROM DBOnVerify WHERE login = :login");
            deleteQuery.setParameter("login", user.getLogin());
            deleteQuery.executeUpdate();
    
            // Добавляем запись в operations
            DBOperation operation = new DBOperation();
            operation.setAuthorLogin(client.getLogin());
            operation.setAuthorName(client.getName());
            operation.setTargetType("Client");
            operation.setTarget(user.getLogin());
            session.save(operation);
    
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static void removeClient(Session session, Client client, Client user) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
    
            // Удаляем пользователя из on_verify
            Query deleteQuery = session.createQuery("DELETE FROM DBOnVerify WHERE login = :login");
            deleteQuery.setParameter("login", user.getLogin());
            deleteQuery.executeUpdate();
    
            // Добавляем запись в operations
            DBOperation operation = new DBOperation();
            operation.setAuthorLogin(client.getLogin());
            operation.setAuthorName(client.getName());
            operation.setOperationType("Delete");
            operation.setTargetType("Client");
            operation.setTarget(user.getLogin());
            session.save(operation);
    
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static void editClient(Session session, Client client, TransferCode code, Client oldUser, Client newUser) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
    
            // Получаем клиента из базы данных
            Query<DBClient> query = session.createQuery("FROM DBClient WHERE login = :login", DBClient.class);
            query.setParameter("login", oldUser.getLogin());
            DBClient dbClient = query.uniqueResult();
    
            if (dbClient == null) {
                System.out.println("User not found!");
                return;
            }
    
            String whatChanged = "";
            switch (code.getCode()) {
                case "name":
                    dbClient.setName(newUser.getName());
                    whatChanged = code.getCode() + ": " + oldUser.getName() + " -> " + newUser.getName();
                    break;
                case "isAdmin":
                    dbClient.setIsAdmin(newUser.getAdmin());
                    whatChanged = code.getCode() + ": " + oldUser.getAdmin() + " -> " + newUser.getAdmin();
                    break;
                default:
                    System.out.println("Invalid field to update!");
                    return;
            }
    
            // Сохраняем изменения
            session.update(dbClient);
    
            // Добавляем запись в operations
            DBOperation operation = new DBOperation();
            operation.setAuthorLogin(client.getLogin());
            operation.setAuthorName(client.getName());
            operation.setOperationType("Edit");
            operation.setTarget(newUser.getLogin());
            operation.setInformation(whatChanged);
            session.save(operation);
    
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public static OperationArrayList getOperations(Session session) {
        Transaction transaction = null;
        OperationArrayList list = new OperationArrayList();
        try {
            transaction = session.beginTransaction();
    
            // Получаем список операций из базы данных
            Query<DBOperation> query = session.createQuery("FROM DBOperation", DBOperation.class);
            List<DBOperation> operations = query.getResultList();
    
            // Конвертируем DBOperation в Operation и добавляем в список
            for (DBOperation dbOperation : operations) {
                Operation operation = Factory.makeOperation();
                operation.setLogin(dbOperation.getAuthorLogin());
                operation.setName(dbOperation.getAuthorName());
                operation.setOperationType(dbOperation.getOperationType());
                operation.setTargetType(dbOperation.getTargetType());
                operation.setTarget(dbOperation.getTarget());
                operation.setInfo(dbOperation.getInformation());
                list.getList().add(operation);
            }
    
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
        return list;
    }
}
