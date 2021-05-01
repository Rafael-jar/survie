package be.raffon.survie.utils;

import be.raffon.survie.Bank;

import java.util.Comparator;

public class BankSorter implements Comparator<Bank> {
    @Override
    public int compare(Bank o1, Bank o2) {
        //return survie.bank.get(o2) - survie.bank.get(o1);
        return o2.getEmeralds() - o1.getEmeralds();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
