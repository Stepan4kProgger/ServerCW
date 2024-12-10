import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class Testing {
    @Test
    public void testJDBC() throws SQLException {
        JDBC.JDBC_class.connect();
        Assertions.assertNotNull(JDBC.JDBC_class.getConnection());
    }

    @Test
    public void testFactory() {
        res.common.Worker worker = res.common.Factory.makeWorker();
        Assertions.assertNotNull(worker);
    }

    @Test
    public void testClone() {
        res.common.Product product = res.common.Factory.makeProduct();
        product.setName("test");
        res.common.Product productClone = product.makeClone();
        Assertions.assertEquals(product.getName(), productClone.getName());
    }
}
