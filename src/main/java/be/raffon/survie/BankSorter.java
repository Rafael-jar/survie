package be.raffon.survie;

import java.util.Comparator;
import java.util.UUID;

public class BankSorter implements Comparator<UUID> {
    @Override
    public int compare(UUID o1, UUID o2) {
        return survie.bank.get(o2) - survie.bank.get(o1);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
