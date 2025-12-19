package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Order;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;



@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {

    private ProfileDao profileDao;
    private ShoppingCartDao shoppingCartDao;


    @Autowired
    public MySqlOrderDao(DataSource dataSource, ProfileDao profileDao, ShoppingCartDao shoppingCartDao) {
        super(dataSource);
        this.profileDao = profileDao;
        this.shoppingCartDao = shoppingCartDao;
    }

    @Override
    public Order create(int userId) {
        ShoppingCart shoppingCart = shoppingCartDao.getByUserId(userId);
        Profile profile = profileDao.getByUserId(userId);

        String orderQuery = " INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";

        String orderLineItemQuery = " INSERT INTO order_line_items (order_id, product_id, sales_price, quantity, discount) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime orderTime = LocalDateTime.now();

            statement.setInt(1, userId);
            statement.setTimestamp(2, Timestamp.valueOf(orderTime));
            statement.setString(3, profile.getAddress());
            statement.setString(4, profile.getCity());
            statement.setString(5, profile.getState());
            statement.setString(6, profile.getZip());
            statement.setBigDecimal(7, BigDecimal.ZERO);

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int orderId = generatedKeys.getInt(1);
                try (PreparedStatement preparedStatement = connection.prepareStatement(orderLineItemQuery)) {
                    for (ShoppingCartItem item : shoppingCart.getItems().values()) {
                        preparedStatement.setInt(1, orderId);
                        preparedStatement.setInt(2, item.getProductId());
                        preparedStatement.setBigDecimal(3, item.getProduct().getPrice());
                        preparedStatement.setInt(4, item.getQuantity());
                        preparedStatement.setBigDecimal(5, item.getDiscountPercent());

                        preparedStatement.executeUpdate();
                    }
                }
                shoppingCartDao.deleteCart(userId);

                return new Order(
                        orderId,
                        userId,
                        orderTime,
                        profile.getAddress(),
                        profile.getCity(),
                        profile.getState(),
                        profile.getZip(),
                        BigDecimal.ZERO
                );
            }
            else{
                throw new RuntimeException("Failed to create order.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
