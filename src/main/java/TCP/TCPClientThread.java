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
                    out.writeObject(getProducts(session));
                    boolean noBackRequest = true;
                    while (noBackRequest) {
                        switch (((TransferCode) in.readObject()).getCode()) {
                            case "refresh" -> out.writeObject(getProducts(session));
                            case "add" -> addProduct(session, client, (Product) in.readObject());
                            case "remove" -> removeProduct(session, client, (Product) in.readObject());
                            case "edit" ->
                                    editProduct(session, client, (TransferCode) in.readObject(), (Product) in.readObject(), (Product) in.readObject());
                            case "prod_types" -> out.writeObject(getTypes(session));
                            case "add_type" -> addType(session, (ProductType) in.readObject());
                            case "back" -> noBackRequest = false;
                            case "leave" -> {
                                noBackRequest = false;
                                proceed = false;
                                client = null;
                            }
                        }
                    }
                }
                case "manage_workers" -> {
                    out.writeObject(getWorkers(session));
                    boolean noBackRequest = true;
                    while (noBackRequest) {
                        switch (((TransferCode) in.readObject()).getCode()) {
                            case "refresh" -> out.writeObject(getWorkers(session));
                            case "add" -> addWorker(session, client, (Worker) in.readObject());
                            case "remove" -> removeWorker(session, client, (Worker) in.readObject());
                            case "edit" ->
                                    editWorker(session, client, (TransferCode) in.readObject(), (Worker) in.readObject(), (Worker) in.readObject());
                            case "get_logins" -> out.writeObject(getClients(session));
                            case "back" -> noBackRequest = false;
                            case "leave" -> {
                                noBackRequest = false;
                                proceed = false;
                                client = null;
                            }
                        }
                    }
                }
                case "manage_users" -> {
                    out.writeObject(getClients(session));
                    boolean noBackRequest = true;
                    while (noBackRequest) {
                        switch (((TransferCode) in.readObject()).getCode()) {
                            case "refresh" -> out.writeObject(getClients(session));
                            case "add" -> addClient(session, client, (Client) in.readObject());
                            case "remove" -> removeClient(session, client, (Client) in.readObject());
                            case "edit" ->
                                    editClient(session, client, (TransferCode) in.readObject(), (Client) in.readObject(), (Client) in.readObject());
                            case "get_workers" -> out.writeObject(getWorkers(session));
                            case "get_unverified" -> out.writeObject(getUnverified(session));
                            case "back" -> noBackRequest = false;
                            case "leave" -> {
                                noBackRequest = false;
                                proceed = false;
                                client = null;
                            }
                        }
                    }
                }
                case "see_all_operations" -> {
                    out.writeObject(getOperations(session));
                    boolean noBackRequest = true;
                    while (noBackRequest) {
                        switch (((TransferCode) in.readObject()).getCode()) {
                            case "refresh" -> out.writeObject(getOperations(session));
                            case "back" -> noBackRequest = false;
                            case "leave" -> {
                                noBackRequest = false;
                                proceed = false;
                                client = null;
                            }
                        }
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
