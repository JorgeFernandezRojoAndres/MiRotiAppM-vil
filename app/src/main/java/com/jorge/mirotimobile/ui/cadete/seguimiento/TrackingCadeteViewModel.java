package com.jorge.mirotimobile.ui.cadete.seguimiento;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jorge.mirotimobile.model.DetallePedidoInfoDTO;
import com.jorge.mirotimobile.model.PedidoDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackingCadeteViewModel extends ViewModel {

    public enum EstadoEntrega {
        ESPERANDO,
        EN_CAMINO,
        ENTREGADO
    }

    private final MutableLiveData<List<PedidoDTO>> entregas = new MutableLiveData<>();
    private final MutableLiveData<PedidoDTO> entregaActual = new MutableLiveData<>();
    private final MutableLiveData<EstadoEntrega> estadoEntrega = new MutableLiveData<>(EstadoEntrega.ESPERANDO);

    public TrackingCadeteViewModel() {
        List<DetallePedidoInfoDTO> detalles = new ArrayList<>();
        DetallePedidoInfoDTO detalle = new DetallePedidoInfoDTO();
        detalle.setPlato("Ensalada mixta");
        detalle.setCantidad(1);
        detalle.setSubtotal(3500);
        detalles.add(detalle);

        PedidoDTO inicial = new PedidoDTO();
        inicial.setId(908);
        inicial.setEstado("EN_CAMINO");
        inicial.setTotal(3500);
        inicial.setDetalles(detalles);
        inicial.setFechaHora("2025-12-13T21:30:00");

        entregas.setValue(Arrays.asList(inicial));
        entregaActual.setValue(inicial);
        estadoEntrega.setValue(mapEstado(inicial.getEstado()));
    }

    public LiveData<List<PedidoDTO>> getEntregas() {
        return entregas;
    }

    public LiveData<PedidoDTO> getPedido() {
        return entregaActual;
    }

    public LiveData<EstadoEntrega> getEstadoEntrega() {
        return estadoEntrega;
    }

    public void iniciarEntrega() {
        estadoEntrega.setValue(EstadoEntrega.EN_CAMINO);
    }

    public void marcarEntregado() {
        EstadoEntrega current = estadoEntrega.getValue();
        if (current == EstadoEntrega.ENTREGADO) return;
        estadoEntrega.setValue(EstadoEntrega.ENTREGADO);
        PedidoDTO actual = entregaActual.getValue();
        if (actual != null) {
            actual.setEstado("ENTREGADO");
            entregaActual.setValue(actual);
        }
    }

    private EstadoEntrega mapEstado(String estado) {
        if ("EN_CAMINO".equalsIgnoreCase(estado)) {
            return EstadoEntrega.EN_CAMINO;
        }
        if ("ENTREGADO".equalsIgnoreCase(estado)) {
            return EstadoEntrega.ENTREGADO;
        }
        return EstadoEntrega.ESPERANDO;
    }
}
