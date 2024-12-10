import Hibernate.HibernateUtil;
import JDBC.JDBC_class;
import TCP.TCPClientThread;
import org.hibernate.Session;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Scanner;

import static TCP.TCPClientThread.clientAmount;

public class Server {
    private static final int PORT = 55555;

    private static boolean onFirstTime() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Пройдите по шагам для создания бд\n" +
                "Введите требуемый логин стартовой учётной записи");
        final String req_answer = "admin";
        if (scanner.next().equals(req_answer)){
            System.out.println("Введите требуемый пароль стартовой учётной записи");
            if (scanner.next().equals(req_answer)) {
                try {
                    JDBC_class.createDB();
                    return true;
                } catch (SQLException e) {
                    System.out.println("Возникла ошибка при создании БД.");
                }
            }
        }
        scanner.close();
        return false;
    }

    private static boolean isDatabaseAvailable() {
        try {
            JDBC_class.connect();
            JDBC_class.close();
        } catch (SQLException e) {
            System.out.println("Работа с SQL невозможна\nПричина: " + e.getMessage());
            if (e.getErrorCode() == 1049)
                if (onFirstTime())
                    return true;
        }
        return false;
    }

    public static void main(String[] args) {
        // Проверяем БД на существование
        if (!isDatabaseAvailable()) {
            System.out.println("Создание БД отменено.");
            return;
        }

        // Открываем сессию

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            ServerSocket socket = null;
            socket = new ServerSocket(PORT);
            System.out.println("Сервер запущен. Адрес: " + socket.getLocalSocketAddress() + '\n' +
                    "К обслуживанию клиентов готов");
            long id = 1;
            while (!socket.isClosed()) {
                Socket connection = socket.accept();
                Thread thread = new Thread(new TCPClientThread(connection, session), "" + id++);
                thread.start();
                System.out.println(++clientAmount + " клиентов сейчас активно");
            }
        } catch (SQLException e) {
            System.out.println("Работа с SQL невозможна\nПричина: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Возникли проблемы при работе с протоколом");
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("Сервер остановлен вручную");
        } finally {
            HibernateUtil.shutdown();
        }
    }
}