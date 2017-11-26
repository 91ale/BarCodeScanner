package it.ale.barcodescanner;

/**
 * Created by hale on 22/10/2017.
 */

public class Product {
    private int id;
    private String bc;
    private String nome;
    private double prezzoa;
    private double prezzov;
    private String marca;

    public Product(int id, String bc, String nome, double prezzoa, double prezzov, String marca) {
        this.id = id;
        this.bc = bc;
        this.nome = nome;
        this.prezzoa = prezzoa;
        this.prezzov = prezzov;
        this.marca = marca;
    }

    public int getId() {
        return id;
    }

    public String getbc() {
        return bc;
    }

    public String getnome() {
        return nome;
    }

    public double getprezzoa() {
        return prezzoa;
    }

    public double getprezzov() { return prezzov; }

    public String getmarca() {
        return marca;
    }

}