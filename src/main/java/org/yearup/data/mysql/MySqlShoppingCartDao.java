package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        String sql = """
                SELECT * FROM shopping_cart sc
                JOIN products p on sc.product_id = p.product_id
                WHERE user_id = ?""";
        ShoppingCart cart = new ShoppingCart();
        try(Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, userId);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    do{
                        int productId = resultSet.getInt("product_id");
                        int quantity = resultSet.getInt("quantity");
                        String name = resultSet.getString("name");
                        BigDecimal price = resultSet.getBigDecimal("price");
                        int categoryId = resultSet.getInt("category_id");
                        String description = resultSet.getString("description");
                        String subCategory = resultSet.getString("subcategory");
                        String imageUrl = resultSet.getString("image_url");
                        int stock = resultSet.getInt("stock");
                        boolean isFeatured = resultSet.getBoolean("featured");

                        ShoppingCartItem item = new ShoppingCartItem();
                        item.setProduct(new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl));
                        item.setQuantity(quantity);
                        cart.add(item);
                    }while(resultSet.next());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cart;
    }

    @Override
    public ShoppingCart addProduct(int userId, int productId) {
        String sql = """
                INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)
                ON DUPLICATE KEY UPDATE quantity = quantity + 1;
                """;
        try(Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, productId);
            int rows = preparedStatement.executeUpdate();
            if(rows == 0) System.out.println("No rows have been updated.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return getByUserId(userId);
    }

    @Override
    public void updateCart(int userId, int productId, int quantity) {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE product_id = ? AND user_id = ?";
        try(Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, quantity);
            preparedStatement.setInt(2, productId);
            preparedStatement.setInt(3,userId);
            int rows = preparedStatement.executeUpdate();
            if(rows == 0) throw new SQLException("Update failed, no rows affected!");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCart(int userId) {
        String sql = " DELETE FROM shopping_cart WHERE user_id = ?";
        try(Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, userId);
            int rows = preparedStatement.executeUpdate();
            if(rows == 0) System.out.println("No rows have been deleted");
            else System.out.println("Cart has been cleared!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
