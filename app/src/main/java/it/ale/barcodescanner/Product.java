package it.ale.barcodescanner;

/**
 * Classe contenente le informazioni relative ad 1 prodotto
 */


public class Product {
    private int id;
    private String bc;
    private String nome;
    private double prezzoa;
    private double prezzov;
    private String marca;

    //assegnazione valori agli attributi della classe (passati alla creazione della classe)

    public Product(int id, String bc, String nome, double prezzoa, double prezzov, String marca) {
        this.id = id;
        this.bc = bc;
        this.nome = nome;
        this.prezzoa = prezzoa;
        this.prezzov = prezzov;
        this.marca = marca;
    }

    //metodi utilizzati per ottenere le info relative ad un prodotto specifico
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