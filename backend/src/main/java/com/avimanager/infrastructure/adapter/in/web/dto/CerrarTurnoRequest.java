package com.avimanager.infrastructure.adapter.in.web.dto;

/**
 * DTO de entrada HTTP. Deliberadamente usa tipos "envueltos" (Double/Integer,
 * no double/int) para poder distinguir "campo no enviado" (null) de "campo en cero",
 * ya que esa distincion es la que valida RN-05.
 */
public class CerrarTurnoRequest {

    private Double consumoAlimentoKg;
    private Integer cantidadBajas;
    private String causaProbableMortalidad;
    private Integer produccionHuevosBandejas;
    private String novedades;

    public Double getConsumoAlimentoKg() {
        return consumoAlimentoKg;
    }

    public void setConsumoAlimentoKg(Double consumoAlimentoKg) {
        this.consumoAlimentoKg = consumoAlimentoKg;
    }

    public Integer getCantidadBajas() {
        return cantidadBajas;
    }

    public void setCantidadBajas(Integer cantidadBajas) {
        this.cantidadBajas = cantidadBajas;
    }

    public String getCausaProbableMortalidad() {
        return causaProbableMortalidad;
    }

    public void setCausaProbableMortalidad(String causaProbableMortalidad) {
        this.causaProbableMortalidad = causaProbableMortalidad;
    }

    public Integer getProduccionHuevosBandejas() {
        return produccionHuevosBandejas;
    }

    public void setProduccionHuevosBandejas(Integer produccionHuevosBandejas) {
        this.produccionHuevosBandejas = produccionHuevosBandejas;
    }

    public String getNovedades() {
        return novedades;
    }

    public void setNovedades(String novedades) {
        this.novedades = novedades;
    }
}
