package com.jorge.mirotimobile.ui.cadete.seguimiento;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.mirotimobile.model.DetallePedidoInfoDTO;
import com.jorge.mirotimobile.model.PedidoDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackingCadeteViewModel extends AndroidViewModel {

    public enum EstadoEntrega {
        ESPERANDO,
        EN_CAMINO,
        ENTREGADO
    }

    private final MutableLiveData<List<PedidoDTO>> entregas = new MutableLiveData<>();
    private final MutableLiveData<PedidoDTO> entregaActual = new MutableLiveData<>();
    private final MutableLiveData<EstadoEntrega> estadoEntrega = new MutableLiveData<>(EstadoEntrega.ESPERANDO);

    public TrackingCadeteViewModel(@NonNull Application application) {
        super(application);
        inicializarDatosPrueba();
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
        if (!puedeMarcarEntregado()) {
            return;
        }
        
        actualizarEstadoEntregado();
    }

    private void inicializarDatosPrueba() {
        PedidoDTO inicial = crearPedidoPrueba();
        entregas.setValue(Arrays.asList(inicial));
        entregaActual.setValue(inicial);
        estadoEntrega.setValue(mapEstado(inicial.getEstado()));
    }
    
    private PedidoDTO crearPedidoPrueba() {
        List<DetallePedidoInfoDTO> detalles = crearDetallesPrueba();
        
        PedidoDTO pedido = new PedidoDTO();
        pedido.setId(908);
        pedido.setEstado("EN_CAMINO");
        pedido.setTotal(3500);
        pedido.setDetalles(detalles);
        pedido.setFechaHora("2025-12-13T21:30:00");
        
        return pedido;
    }
    
    private List<DetallePedidoInfoDTO> crearDetallesPrueba() {
        List<DetallePedidoInfoDTO> detalles = new ArrayList<>();
        DetallePedidoInfoDTO detalle = new DetallePedidoInfoDTO();
        detalle.setPlato("Ensalada mixta");
        detalle.setCantidad(1);
        detalle.setSubtotal(3500);
        detalles.add(detalle);
        return detalles;
    }
    
    private boolean puedeMarcarEntregado() {
        EstadoEntrega current = estadoEntrega.getValue();
        return current != EstadoEntrega.ENTREGADO;
    }
    
    private void actualizarEstadoEntregado() {
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
