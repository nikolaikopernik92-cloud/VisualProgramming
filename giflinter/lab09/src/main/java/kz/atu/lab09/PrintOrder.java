package kz.atu.lab09;

public record PrintOrder(String id, String name, String email,
                         int pages, int copies, String format) { }
