package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@CrossOrigin
@RequestMapping("orders")
@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
public class OrdersController {

    private OrderDao ordersDao;
    private UserDao userDao;


    public OrdersController(OrderDao ordersDao, UserDao userDao) {
        this.ordersDao = ordersDao;
        this.userDao = userDao;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order checkOutOrder(Principal principal) {
        try {
            int userId = getUserId(principal);
            return ordersDao.create(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    private int getUserId(Principal principal) {
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        return user.getId();
    }
}
