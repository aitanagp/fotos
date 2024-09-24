package com.example.fotos;

public class Inspeccion {
    private String estacion;
    private String anyo;
    private String codigo;
    private String empresa;
    private String matricula;
    private String fecha_inicio;
    private String fecha_fin;
    private String estado;

    public String getEstacion() {
        return estacion;
    }

    public void setEstacion(String estacion) {
        this.estacion = estacion;
    }
    public String getAnyo() {
        return anyo;
    }


    public String getCodigo() {
        return codigo;
    }
    public String getEmpresa() {
        return empresa;
    }
    public String getMatricula() {
        return matricula;
    }
    public String getFecha_inicio() {
        return fecha_inicio;
    }
    public String getFecha_fin() {
        return fecha_fin;
}

    public void setAnyo(String anyo) {
        this.anyo = anyo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
    public void setFecha_inicio(String fecha_inicio) {
        this.fecha_inicio = fecha_inicio;
    }
    public void setFecha_fin(String fecha_fin) {
        this.fecha_fin = fecha_fin;
    }
}
