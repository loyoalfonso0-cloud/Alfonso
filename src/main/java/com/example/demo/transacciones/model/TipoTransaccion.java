package com.example.demo.transacciones.model;

public enum TipoTransaccion {
    INGRESO("Ingreso"),
    EGRESO("Egreso"),
    TRANSFERENCIA("Transferencia"),
    AJUSTE("Ajuste"),
    PAGO_SERVICIO("Pago de Servicio"),
    PAGO_IMPUESTO("Pago de Impuesto"),
    PAGO_TASA("Pago de Tasa"),
    PAGO_MULTA("Pago de Multa"),
    DEVOLUCION("Devolución"),
    COMISION("Comisión"),
    INTERES("Interés"),
    DESCUENTO("Descuento");

    private final String descripcion;

    TipoTransaccion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
