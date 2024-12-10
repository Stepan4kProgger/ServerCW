package TCP;


import JDBC.JDBC_class;

import org.hibernate.Session;
import org.hibernate.Transaction;
import res.common.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Statement;

import static Hibernate.Queries.*;

public class TCPClientThread implements Runnable {
    public static int clientAmount = 0;
    private final Session session;
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Client client;

    public TCPClientThread(Socket socket, Session session) throws SQLException {
        this.socket = socket;
        this.session = session;
    }

    private void onLoginState() throws IOException, ClassNotFoundException {
        Client account = (Client) in.readObject();
        String answer = logIn(session, account.getLogin(), account.getPassword());
        if ("01".contains(answer)) {
            try {
                client = getClientByLogin(session, account.getLogin());
            } catch (Exception e) {
                answer = "err";
            }
        }
        out.writeObject(new TransferCode(answer));
    }

    private void onRegisterState() throws IOException, ClassNotFoundException {
        Client account = (Client) in.readObject();
        try {
            if (isLoginAvailable(session, account.getLogin())) {
                addNewAccountReg(session, account.getLogin(), account.getPassword());
                out.writeObject(new TransferCode("success"));
            } else
                out.writeObject(new TransferCode("bad_login"));
        } catch (Exception e) {
            e.printStackTrace();
            out.writeObject(new TransferCode("err"));
        }
    }

    private void afterLoginState() throws IOException, ClassNotFoundException {
        boolean proceed = true;
        out.writeObject(client);
        while (proceed)
            switch (((TransferCode) in.readObject()).getCode()) {
                case "leave" -> {
                    proceed = false;
                    client = null;
                }
                case "change_password" -> {
                    client.setPassword(((Client) in.readObject()).getPassword());
                    try {
                        changePassword(session, client);
                    } catch (Exception ignored) {
                    }
                }
                case "see_workers" -> {
                    try {
                        out.writeObject(getWorkers(session));
                    } catch (Exception ignored) {
                    }
                }
                case "see_operations_user" -> {
                    try {
                        out.writeObject(seeOperationsAtProducts(session));
                    } catch (Exception ignored) {
                    }
                }
                case "manage_products" -> {
                    try {
                        out.writeObject(getProducts(statement));
                        boolean noBackRequest = true;
                        while (noBackRequest) {
                            switch (((TransferCode) in.readObject()).getCode()) {
                                case "refresh" -> out.writeObject(getProducts(statement));
                                case "add" -> addProduct(statement, client, (Product) in.readObject());
                                case "remove" -> removeProduct(statement, client, (Product) in.readObject());
                                case "edit" ->
                                        editProduct(statement, client, (TransferCode) in.readObject(), (Product) in.readObject(), (Product) in.readObject());
                                case "prod_types" -> out.writeObject(getTypes(statement));
                                case "add_type" -> addType(statement, (ProductType) in.readObject());
                                case "back" -> noBackRequest = false;
                                case "leave" -> {
                                    noBackRequest = false;
                                    proceed = false;
                                    client = null;
                                }
                            }
                        }
                    } catch (SQLException ignored) {
                    }
                }
                case "manage_workers" -> {
                    try {
                        out.writeObject(getWorkers(session));
                        boolean noBackRequest = true;
                        while (noBackRequest) {
                            switch (((TransferCode) in.readObject()).getCode()) {
                                case "refresh" -> out.writeObject(getWorkers(session));
                                case "add" -> addWorker(statement, client, (Worker) in.readObject());
                                case "remove" -> removeWorker(statement, client, (Worker) in.readObject());
                                case "edit" ->
                                        editWorker(statement, client, (TransferCode) in.readObject(), (Worker) in.readObject(), (Worker) in.readObject());
                                case "get_logins" -> out.writeObject(getClients(statement));
                                case "back" -> noBackRequest = false;
                                case "leave" -> {
                                    noBackRequest = false;
                                    proceed = false;
                                    client = null;
                                }
                            }
                        }
                    } catch (SQLException ignored) {
                    }
                }
                case "manage_users" -> {
                    try {
                        out.writeObject(getClients(statement));
                        boolean noBackRequest = true;
                        while (noBackRequest) {
                            switch (((TransferCode) in.readObject()).getCode()) {
                                case "refresh" -> out.writeObject(getClients(statement));
                                case "add" -> addClient(statement, client, (Client) in.readObject());
                                case "remove" -> removeClient(statement, client, (Client) in.readObject());
                                case "edit" ->
                                        editClient(statement, client, (TransferCode) in.readObject(), (Client) in.readObject(), (Client) in.readObject());
                                case "get_workers" -> out.writeObject(getWorkers(session));
                                case "get_unverified" -> out.writeObject(getUnverified(statement));
                                case "back" -> noBackRequest = false;
                                case "leave" -> {
                                    noBackRequest = false;
                                    proceed = false;
                                    client = null;
                                }
                            }
                        }
                    } catch (SQLException ignored) {
                    }
                }
                case "see_all_operations" -> {
                    try {
                        out.writeObject(getOperations(statement));
                        boolean noBackRequest = true;
                        while (noBackRequest) {
                            switch (((TransferCode) in.readObject()).getCode()) {
                                case "refresh" -> out.writeObject(getOperations(statement));
                                case "back" -> noBackRequest = false;
                                case "leave" -> {
                                    noBackRequest = false;
                                    proceed = false;
                                    client = null;
                                }
                            }
                        }
                    } catch (SQLException ignored) {
                    }
                }
            }
    }

    @Override
    public void run() {
        System.out.println("Подключен клиент (id: " + Thread.currentThread().getName() + ')');
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            while (!socket.isClosed()) {
                switch (((TransferCode) in.readObject()).getCode()) {
                    case "login" -> onLoginState();
                    case "register" -> {
                        Transaction tx = session.beginTransaction();
                        onRegisterState();
                        tx.commit();
                    }
                    case "after_login" -> afterLoginState();
                    case "exit" -> socket.close();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Возникли проблемы с обработкой данных сокетом (id: " + Thread.currentThread().getName() + ')');
        } finally {
            try {
                if (!socket.isClosed())
                    socket.close();
                in.close();
                out.close();
            } catch (IOException | NullPointerException e) {
                System.out.println("Невозможно закрыть подключение или потоки ввода/вывода (id: " + Thread.currentThread().getName() + ')');
            }
        }
        System.out.println("Отключился клиент (id: " + Thread.currentThread().getName() + ')');
        System.out.println(--clientAmount + " клиентов сейчас активно");
    }
}
