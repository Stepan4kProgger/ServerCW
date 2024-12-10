package JDBC;

import res.common.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Queries {
    public static void makeDB(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE DATABASE IF NOT EXISTS `shopdatabase` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
                USE `shopdatabase`;

                -- Дамп структуры для таблица shopdatabase.on_verify
                CREATE TABLE IF NOT EXISTS `on_verify` (
                  `login` varchar(32) NOT NULL,
                  `password` varchar(32) NOT NULL,
                  PRIMARY KEY (`login`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

                -- Экспортируемые данные не выделены.

                -- Дамп структуры для таблица shopdatabase.operations
                CREATE TABLE IF NOT EXISTS `operations` (
                  `author_login` varchar(64) NOT NULL,
                  `author_name` varchar(64) DEFAULT NULL,
                  `operation_type` enum('Добавление','Удаление','Редактирование') NOT NULL DEFAULT 'Добавление',
                  `target_type` enum('Сотрудник','Товар','Пользователь') NOT NULL DEFAULT 'Товар',
                  `target` varchar(64) NOT NULL,
                  `information` varchar(128) DEFAULT NULL,
                  KEY `FK_operations_users` (`author_login`),
                  KEY `FK_operations_worker` (`author_name`),
                  CONSTRAINT `FK_operations_users` FOREIGN KEY (`author_login`) REFERENCES `users` (`login`),
                  CONSTRAINT `FK_operations_worker` FOREIGN KEY (`author_name`) REFERENCES `worker` (`name`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

                -- Экспортируемые данные не выделены.

                -- Дамп структуры для таблица shopdatabase.product
                CREATE TABLE IF NOT EXISTS `product` (
                  `id` int NOT NULL AUTO_INCREMENT,
                  `prod_type` varchar(32) NOT NULL,
                  `name` varchar(64) NOT NULL,
                  `manufacturer` varchar(64) NOT NULL,
                  `price` float NOT NULL,
                  PRIMARY KEY (`id`),
                  KEY `FK_product_types_of_product` (`prod_type`),
                  CONSTRAINT `FK_product_types_of_product` FOREIGN KEY (`prod_type`) REFERENCES `types_of_product` (`prod_type`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

                -- Экспортируемые данные не выделены.

                -- Дамп структуры для таблица shopdatabase.types_of_product
                CREATE TABLE IF NOT EXISTS `types_of_product` (
                  `prod_type` varchar(32) NOT NULL,
                  `descriptipon` varchar(128) DEFAULT NULL,
                  PRIMARY KEY (`prod_type`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

                -- Экспортируемые данные не выделены.

                -- Дамп структуры для таблица shopdatabase.users
                CREATE TABLE IF NOT EXISTS `users` (
                  `login` varchar(32) NOT NULL,
                  `password` varchar(32) NOT NULL,
                  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                  `isAdmin` int NOT NULL DEFAULT '0',
                  PRIMARY KEY (`login`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

                -- Экспортируемые данные не выделены.

                -- Дамп структуры для таблица shopdatabase.worker
                CREATE TABLE IF NOT EXISTS `worker` (
                  `name` varchar(64) NOT NULL,
                  `age` int NOT NULL,
                  `login` varchar(32) DEFAULT NULL,
                  `post` varchar(32) NOT NULL,
                  PRIMARY KEY (`name`),
                  KEY `FK_worker_users` (`login`),
                  CONSTRAINT `FK_worker_users` FOREIGN KEY (`login`) REFERENCES `users` (`login`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
                """);
        statement.executeUpdate("INSERT INTO users VALUES (login='admin', password='admin', isAdmin=1)");
    }

    private static boolean isLoginInUsers(Statement statement, String login) throws SQLException {
        ResultSet res_users = statement.executeQuery("SELECT COUNT(*) FROM users WHERE login='" + login + "'");
        res_users.next();
        return res_users.getInt("COUNT(*)") == 1;
    }

    private static boolean isLoginOnVerify(Statement statement, String login) throws SQLException {
        ResultSet res_on_verify = statement.executeQuery("SELECT COUNT(*) FROM on_verify WHERE login='" + login + "'");
        res_on_verify.next();
        return res_on_verify.getInt("COUNT(*)") == 1;
    }

    public static boolean isLoginAvailable(Statement statement, String login) throws SQLException {
        return !(isLoginInUsers(statement, login) || isLoginOnVerify(statement, login));
    }

    public static void addNewAccountReg(Statement statement, String login, String password) throws SQLException {
        statement.executeUpdate("INSERT INTO on_verify (`login`, `password`) VALUES ('" + login + "', '" + password + "')");
    }

    public static String logIn(Statement statement, String login, String password) {
        try {
            ResultSet res = statement.executeQuery("SELECT COUNT(*) FROM users " +
                    "WHERE login = '" + login + "' AND password = '" + password + "'");
            if (res.next()) {
                if (res.getInt("COUNT(*)") > 0) {
                    res.close();
                    res = statement.executeQuery("SELECT isAdmin FROM users WHERE login = '" + login + "'");
                    if (res.next())
                        return "" + res.getInt("isAdmin");
                } else {
                    res.close();
                    if (!isLoginInUsers(statement, login)) {
                        if (isLoginOnVerify(statement, login))
                            return "unverified";
                        return "bad_login";
                    }
                    return "bad_password";
                }
            }
        } catch (SQLException e) {
            return "err";
        }
        return "err";
    }

    public static Client getClientByLogin(Statement statement, String login) throws SQLException {
        ResultSet res = statement.executeQuery("SELECT password, name, isAdmin FROM users WHERE login = '" + login + "'");
        res.next();
        Client client = Factory.makeClient();
        client.setLogin(login);
        client.setPassword(res.getString("password"));
        client.setName(res.getString("name"));
        client.setAdmin(res.getInt("isAdmin"));
        return client;
    }

    public static void changePassword(Statement statement, Client client) throws SQLException {
        statement.executeUpdate("UPDATE users SET password='" + client.getPassword() + "' WHERE login='" + client.getLogin() + "'");
    }

    public static WorkerArrayList getWorkers(Statement statement) throws SQLException {
        ResultSet res = statement.executeQuery("SELECT * FROM worker");
        WorkerArrayList list = new WorkerArrayList();
        while (res.next()) {
            Worker worker = Factory.makeWorker();
            worker.setName(res.getString("name"));
            worker.setAge(res.getInt("age"));
            worker.setLogin(res.getString("login"));
            worker.setPost(res.getString("post"));
            list.getList().add(worker);
        }
        return list;
    }

    public static OperationArrayList seeOperationsAtProducts(Statement statement) throws SQLException {
        ResultSet res = statement.executeQuery("SELECT author_name, operation_type, target, information FROM operations WHERE target_type='Product'");
        OperationArrayList list = new OperationArrayList();
        while (res.next()) {
            Operation operation = Factory.makeOperation();
            operation.setName(res.getString("author_name"));
            operation.setOperationType(res.getString("operation_type"));
            operation.setTarget(res.getString("target"));
            operation.setInfo(res.getString("information"));
            list.getList().add(operation);
        }
        return list;
    }

    public static ProductArrayList getProducts(Statement statement) throws SQLException {
        ResultSet res = statement.executeQuery("SELECT * FROM product");
        ProductArrayList list = new ProductArrayList();
        while (res.next()) {
            Product product = Factory.makeProduct();
            product.setId(res.getInt("id"));
            product.setProdType(res.getString("prod_type"));
            product.setName(res.getString("name"));
            product.setManufacturer(res.getString("manufacturer"));
            product.setAmount(res.getInt("amount"));
            product.setPrice(res.getFloat("price"));
            list.getList().add(product);
        }
        return list;
    }

    public static void addProduct(Statement statement, Client client, Product product) throws SQLException {
        statement.executeUpdate("INSERT INTO product (prod_type, name, manufacturer, amount, price) VALUES (" + product.toQueryString() + ")");
        statement.executeUpdate("INSERT INTO operations (author_login, author_name, target) VALUES ('" + client.getLogin() + "', '" + client.getName() + "', '" + product.getName() + "')");
    }

    public static void removeProduct(Statement statement, Client client, Product product) throws SQLException {
        statement.executeUpdate("DELETE FROM product WHERE id=" + product.getId());
        statement.executeUpdate("INSERT INTO operations (author_login, author_name, operation_type, target) VALUES ('" + client.getLogin() + "', '" + client.getName() + "', 'Delete', '" + product.getName() + "')");
    }

    public static void editProduct(Statement statement, Client client, TransferCode code, Product oldProduct, Product newProduct) throws SQLException {
        String set = "";
        String what_changed = switch (code.getCode()) {
            case "prod_type" -> {
                set = code.getCode() + "='" + newProduct.getProdType() + '\'';
                yield code.getCode() + ": " + oldProduct.getProdType() + " -> " + newProduct.getProdType();
            }
            case "name" -> {
                set = code.getCode() + "='" + newProduct.getName() + '\'';
                yield code.getCode() + ": " + oldProduct.getName() + " -> " + newProduct.getName();
            }
            case "manufacturer" -> {
                set = code.getCode() + "='" + newProduct.getManufacturer() + '\'';
                yield code.getCode() + ": " + oldProduct.getManufacturer() + " -> " + newProduct.getManufacturer();
            }
            case "amount" -> {
                set = code.getCode() + "=" + newProduct.getAmount();
                yield code.getCode() + ": " + oldProduct.getAmount() + " -> " + newProduct.getAmount();
            }
            case "price" -> {
                set = code.getCode() + "=" + newProduct.getPrice();
                yield code.getCode() + ": " + oldProduct.getPrice() + " -> " + newProduct.getPrice();
            }
            default -> "";
        };
        if (what_changed.isEmpty())
            return;
        statement.executeUpdate("UPDATE product SET " + set + " WHERE id=" + oldProduct.getId());
        statement.executeUpdate("INSERT INTO operations (author_login, author_name, operation_type, target, information) VALUES ('" + client.getLogin() + "', '" + client.getName() + "', 'Edit', '" + oldProduct.getName() + "', '" + what_changed + "')");
    }

    public static ProductTypeArrayList getTypes(Statement statement) throws SQLException {
        ResultSet res = statement.executeQuery("SELECT * FROM types_of_product");
        ProductTypeArrayList list = new ProductTypeArrayList();
        while (res.next()) {
            ProductType type = new ProductType();
            type.setProdType(res.getString("prod_type"));
            type.setDescription(res.getString("description"));
            list.getList().add(type);
        }
        return list;
    }

    public static void addType(Statement statement, ProductType type) throws SQLException {
        statement.executeUpdate("INSERT INTO types_of_product (prod_type, description) VALUES ('" + type.getProdType() + "', '" + type.getDescription() + "')");
    }

    public static void addWorker(Statement statement, Client client, Worker worker) throws SQLException {
        statement.executeUpdate("INSERT INTO worker (name, age, post) VALUES (" + worker.toQueryString() + ")");
        statement.executeUpdate("INSERT INTO operations (author_login, author_name, target_type, target) VALUES ('" + client.getLogin() + "', '" + client.getName() + "', 'Worker', '" + worker.getName() + "')");
    }

    public static void removeWorker(Statement statement, Client client, Worker worker) throws SQLException {
        statement.executeUpdate("DELETE FROM worker WHERE name='" + worker.getName() + '\'');
        statement.executeUpdate("INSERT INTO operations (author_login, author_name, operation_type, target_type, target) VALUES ('" + client.getLogin() + "', '" + client.getName() + "', 'Delete', 'Worker', '" + worker.getName() + "')");
    }

    public static void editWorker(Statement statement, Client client, TransferCode code, Worker oldWorker, Worker newWorker) throws SQLException {
        String set = "";
        String what_changed = switch (code.getCode()) {
            case "login" -> {
                set = code.getCode() + "='" + newWorker.getLogin() + '\'';
                yield code.getCode() + ": " + oldWorker.getLogin() + " -> " + newWorker.getLogin();
            }
            case "post" -> {
                set = code.getCode() + "='" + newWorker.getPost() + '\'';
                yield code.getCode() + ": " + oldWorker.getPost() + " -> " + newWorker.getPost();
            }
            default -> "";
        };
        if (what_changed.isEmpty())
            return;
        statement.executeUpdate("UPDATE worker SET " + set + " WHERE name='" + oldWorker.getName() + '\'');
        statement.executeUpdate("INSERT INTO operations (author_login, author_name, operation_type, target, information) VALUES ('" + client.getLogin() + "', '" + client.getName() + "', 'Edit', '" + oldWorker.getName() + "', '" + what_changed + "')");
    }

    public static ClientArrayList getClients(Statement statement) throws SQLException {
        ResultSet res = statement.executeQuery("SELECT * FROM users");
        ClientArrayList list = new ClientArrayList();
        while (res.next()){
            Client client = Factory.makeClient();
            client.setLogin(res.getString("login"));
            client.setPassword(res.getString("password"));
            client.setName(res.getString("name"));
            client.setAdmin(res.getInt("isAdmin"));
            list.getList().add(client);
        }
        return list;
    }

    public static ClientArrayList getUnverified(Statement statement) throws SQLException {
        ResultSet res = statement.executeQuery("SELECT * FROM on_verify");
        ClientArrayList list = new ClientArrayList();
        while (res.next()){
            Client client = Factory.makeClient();
            client.setLogin(res.getString("login"));
            list.getList().add(client);
        }
        return list;
    }

    public static void addClient(Statement statement, Client client, Client user) throws SQLException {
        ResultSet pasSet = statement.executeQuery("SELECT password FROM on_verify WHERE login='" + user.getLogin() + '\'');
        pasSet.next();
        user.setPassword(pasSet.getString("password"));
        user.setAdmin(0);
        statement.executeUpdate("INSERT INTO users VALUES (" + user.toQueryString() + ")");
        statement.executeUpdate("DELETE FROM on_verify WHERE login='" + user.getLogin() + '\'');
        statement.executeUpdate("INSERT INTO operations (author_login, author_name, target_type, target) VALUES ('" + client.getLogin() + "', '" + client.getName() + "', 'Client', '" + user.getLogin() + "')");
    }

    public static void removeClient(Statement statement, Client client, Client user) throws SQLException {
        statement.executeUpdate("DELETE FROM on_verify WHERE login='" + user.getLogin() + '\'');
        statement.executeUpdate("INSERT INTO operations (author_login, author_name, operation_type, target_type, target) VALUES ('" + client.getLogin() + "', '" + client.getName() + "', 'Delete', 'Client', '" + user.getLogin() + "')");
    }

    public static void editClient(Statement statement, Client client, TransferCode code, Client oldUser, Client newUser) throws SQLException {
        String set = "";
        String what_changed = switch (code.getCode()) {
            case "name" -> {
                set = code.getCode() + "='" + newUser.getName() + '\'';
                yield code.getCode() + ": " + oldUser.getName() + " -> " + newUser.getName();
            }
            case "isAdmin" -> {
                set = code.getCode() + "=" + newUser.getAdmin();
                yield code.getCode() + ": " + oldUser.getAdmin() + " -> " + newUser.getAdmin();
            }
            default -> "";
        };
        if (what_changed.isEmpty())
            return;
        statement.executeUpdate("UPDATE users SET " + set + " WHERE login='" + oldUser.getLogin() + '\'');
        statement.executeUpdate("INSERT INTO operations (author_login, author_name, operation_type, target, information) VALUES ('" + client.getLogin() + "', '" + client.getName() + "', 'Edit', '" + newUser.getLogin() + "', '" + what_changed + "')");
    }

    public static OperationArrayList getOperations(Statement statement) throws SQLException {
        ResultSet res = statement.executeQuery("SELECT * FROM operations");
        OperationArrayList list = new OperationArrayList();
        while (res.next()) {
            Operation operation = Factory.makeOperation();
            operation.setLogin(res.getString("author_login"));
            operation.setName(res.getString("author_name"));
            operation.setOperationType(res.getString("operation_type"));
            operation.setTargetType(res.getString("target_type"));
            operation.setTarget(res.getString("target"));
            operation.setInfo(res.getString("information"));
            list.getList().add(operation);
        }
        return list;
    }
}
