package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@CrossOrigin
@RequestMapping("cart")
@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")

public class ShoppingCartController {
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;


    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao)
    {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
    }


    @GetMapping
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            int userId = getUserId(principal);
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }


    @PostMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingCart addProduct(@PathVariable int productId, Principal principal)
    {
        try
        {
            int userId = getUserId(principal);
            shoppingCartDao.addProduct(userId, productId);
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }


    @PutMapping("/products/{productId}")
    public ShoppingCart updateCartItem(@PathVariable int productId, @RequestBody ShoppingCartItem item, Principal principal)
    {
        try
        {
            int userId = getUserId(principal);
            shoppingCartDao.updateCart(userId, productId, item.getQuantity());
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }


    @DeleteMapping
    public ShoppingCart deleteCart(Principal principal)
    {
        try
        {
            int userId = getUserId(principal);
            shoppingCartDao.deleteCart(userId);
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }


    private int getUserId(Principal principal)
    {
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        if(user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        return user.getId();
    }
}
