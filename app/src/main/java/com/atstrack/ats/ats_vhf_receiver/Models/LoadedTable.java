package com.atstrack.ats.ats_vhf_receiver.Models;

public class LoadedTable {
    public int tableNumber;
    public int[] frequenciesLoaded;

    public LoadedTable(int tableNumber, int[] frequenciesLoaded) {
        this.tableNumber = tableNumber;
        this.frequenciesLoaded = frequenciesLoaded;
    }
}
