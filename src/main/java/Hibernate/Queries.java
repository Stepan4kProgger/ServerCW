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

}
