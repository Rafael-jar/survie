package be.raffon.survie.utils;

import be.raffon.survie.Bank;

import java.util.Comparator;

public class BankSorter implements Comparator<Bank> {
    @Override
    public int compare(Bank o1, Bank o2) {
        return o2.getEmeralds() - o1.getEmeralds();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
