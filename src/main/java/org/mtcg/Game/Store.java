package org.mtcg.Game;

import org.mtcg.Cards.Package;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Store {
    List<Package> packages;

    public Store() {
        this.packages = new ArrayList<>();
    }

    public void addPackage(Package p) {
        this.packages.add(p);
    }

    public boolean notEmpty() {
        return !packages.isEmpty();
    }

    public Package getRandPackage() {
        //threadsafe version to get a rand number
        int randomElementIndex = ThreadLocalRandom.current().nextInt(this.packages.size());
        Package p = this.packages.get(randomElementIndex);
        this.packages.remove(p);
        return p;
    }
}
