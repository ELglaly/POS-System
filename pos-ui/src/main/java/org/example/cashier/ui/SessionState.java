package org.example.cashier.ui;

import org.example.cashier.core.entity.Transaction;
import org.example.cashier.core.entity.User;
import org.springframework.stereotype.Component;

/**
 * Transient session state — not persisted.
 * Cleared on logout or app restart.
 */
@Component
public class SessionState {

    private Transaction lastTransaction;
    private User        currentUser;

    public Transaction getLastTransaction()              { return lastTransaction; }
    public void        setLastTransaction(Transaction tx){ this.lastTransaction = tx; }

    public User getCurrentUser()             { return currentUser; }
    public void setCurrentUser(User user)    { this.currentUser = user; }
}
