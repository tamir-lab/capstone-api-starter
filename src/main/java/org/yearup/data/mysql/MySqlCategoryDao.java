package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{

    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories()
    { List<Category> categories = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM categories"
             );
             ResultSet row = preparedStatement.executeQuery()) {
            while (row.next())
            {
                categories.add(mapRow(row));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // get all categories
        return categories;
    }

    @Override
    public Category getById(int categoryId)
    { String sql = "SELECT * FROM categories WHERE category_id = ?";
        try (Connection connection = getConnection();
        ) { PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,categoryId);
            ResultSet row = preparedStatement.executeQuery();
                if(row.next()) {
                    return mapRow(row);
                }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // get category by id
        return null;
    }

    @Override
    public Category create(Category category)
    { String sql = "INSERT INTO Categories (Name,description) VALUES (?,?)";
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1,category.getName());
            preparedStatement.setString(2,category.getDescription());
            int affectedRows = preparedStatement.executeUpdate();

            if(affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if(generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    category.setCategoryId(generatedId);
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // create a new category
        return category;
    }

    @Override
    public void update(int categoryId, Category category)
    {
        String sql = "UPDATE Categories SET name = ?, description = ?  WHERE Category_ID = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category.getName());
            statement.setString(2,category.getDescription());
            statement.setInt(3, categoryId);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // update category
    }

    @Override
    public void delete(int categoryId)
    {
        String sql = "DELETE FROM Categories WHERE Category_ID = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // delete category
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}
