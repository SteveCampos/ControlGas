package gas.apps.steve.controlgas.entities;

import com.google.firebase.database.Exclude;
import com.orm.SugarRecord;

import java.util.HashMap;
import java.util.Map;

import gas.apps.steve.controlgas.utils.Utils;

/**
 * Created by Steve on 24/10/2016.
 */

public class Dispatch extends SugarRecord{
    private String acumuladoAnterior;
    private String acumulateActual;
    private String galones;
    private String kilos;
    private String densidad;
    private String temperatura;
    private long fechaInicio;
    private long fechaFinal;
    /*
    private String fechaInicio;
    private String horaInicio;
    private String fechaFinal;
    private String horaFinal;*/

    private String user;
    private String placa;

    public Dispatch() {
    }

    public Dispatch(String acumuladoAnterior, String acumulateActual, String galones, String kilos, String densidad, String temperatura, long fechaInicio, long fechaFinal) {
        this.acumuladoAnterior = acumuladoAnterior;
        this.acumulateActual = acumulateActual;
        this.galones = galones;
        this.kilos = kilos;
        this.densidad = densidad;
        this.temperatura = temperatura;
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
    }

    public Dispatch(String acumuladoAnterior, String acumulateActual, String galones, String kilos, String densidad, String temperatura, long fechaInicio, long fechaFinal, String user, String placa) {
        this.acumuladoAnterior = acumuladoAnterior;
        this.acumulateActual = acumulateActual;
        this.galones = galones;
        this.kilos = kilos;
        this.densidad = densidad;
        this.temperatura = temperatura;
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
        this.user = user;
        this.placa = placa;
    }

    public String getAcumuladoAnterior() {
        return acumuladoAnterior;
    }

    public void setAcumuladoAnterior(String acumuladoAnterior) {
        this.acumuladoAnterior = acumuladoAnterior;
    }

    public String getAcumulateActual() {
        return acumulateActual;
    }

    public void setAcumulateActual(String acumulateActual) {
        this.acumulateActual = acumulateActual;
    }

    public String getGalones() {
        return galones;
    }

    public void setGalones(String galones) {
        this.galones = galones;
    }

    public String getKilos() {
        return kilos;
    }

    public void setKilos(String kilos) {
        this.kilos = kilos;
    }

    public String getDensidad() {
        return densidad;
    }

    public void setDensidad(String densidad) {
        this.densidad = densidad;
    }

    public String getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }

    public long getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(long fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public long getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(long fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public String getFormatFechaInicio(){
        return Utils.getDate(getFechaInicio());
    }
    public String getFormatFechaFinal(){
        return Utils.getDate(getFechaFinal());
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("acumuladoAnterior", acumuladoAnterior);
        result.put("acumulateActual", acumulateActual);
        result.put("galones", galones);
        result.put("kilos", kilos);
        result.put("densidad", densidad);
        result.put("temperatura", temperatura);
        result.put("fechaInicio", fechaInicio);
        result.put("fechaFinal", fechaFinal);
        result.put("user", user);
        result.put("placa", placa);
        return result;
    }
}
