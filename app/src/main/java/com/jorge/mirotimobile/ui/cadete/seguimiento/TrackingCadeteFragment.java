package com.jorge.mirotimobile.ui.cadete.seguimiento;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.jorge.mirotimobile.R;
import com.jorge.mirotimobile.databinding.FragmentTrackingCadeteBinding;
import com.jorge.mirotimobile.ui.cadete.entregas.EntregasViewModel;

public class TrackingCadeteFragment extends Fragment {

    private FragmentTrackingCadeteBinding binding;
    private EntregasViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentTrackingCadeteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(EntregasViewModel.class);

        observarViewModel();

        binding.btnAbrirMapa.setOnClickListener(v -> viewModel.abrirMapa());
        binding.btnMarcarEntregado.setOnClickListener(v -> viewModel.marcarEntrega());
        binding.btnContactarCliente.setOnClickListener(v -> viewModel.contactarCliente());
    }

    private void observarViewModel() {
        viewModel.getTrackingTitle().observe(getViewLifecycleOwner(), binding.txtTrackingTitle::setText);
        viewModel.getTrackingSubtitle().observe(getViewLifecycleOwner(), binding.txtTrackingSubtitle::setText);
        viewModel.getArrivalTime().observe(getViewLifecycleOwner(), binding.txtArrivalTime::setText);

        viewModel.getProductosTexto().observe(getViewLifecycleOwner(), binding.txtProductos::setText);
        viewModel.getTotalTexto().observe(getViewLifecycleOwner(), binding.txtTotal::setText);
        viewModel.getNotasTexto().observe(getViewLifecycleOwner(), binding.txtNotas::setText);

        viewModel.getEstadoIconRes().observe(getViewLifecycleOwner(), binding.imgEstadoIcon::setImageResource);
        viewModel.getEstadoTitle().observe(getViewLifecycleOwner(), binding.txtEstadoTitle::setText);
        viewModel.getEstadoSubtitle().observe(getViewLifecycleOwner(), binding.txtEstadoDescription::setText);
        viewModel.getMapButtonVisibility().observe(getViewLifecycleOwner(), binding.btnAbrirMapa::setVisibility);

        viewModel.getCardStrokeColor().observe(getViewLifecycleOwner(), binding.cardEstadoPedido::setStrokeColor);
        viewModel.getCardStrokeWidth().observe(getViewLifecycleOwner(), binding.cardEstadoPedido::setStrokeWidth);
        viewModel.getCardBackgroundColor().observe(getViewLifecycleOwner(),
                binding.cardEstadoPedido::setCardBackgroundColor);

        viewModel.getButtonStrokeColor().observe(getViewLifecycleOwner(), binding.btnAbrirMapa::setStrokeColor);
        viewModel.getButtonStrokeWidth().observe(getViewLifecycleOwner(), binding.btnAbrirMapa::setStrokeWidth);
        viewModel.getButtonBackgroundTint().observe(getViewLifecycleOwner(),
                binding.btnAbrirMapa::setBackgroundTintList);

        viewModel.getIconColorFilter().observe(getViewLifecycleOwner(),
                colorFilter -> binding.imgEstadoIcon.setColorFilter(colorFilter.color, colorFilter.mode));

        viewModel.getMensajeError().observe(getViewLifecycleOwner(),
                mensaje -> Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_SHORT).show());
        viewModel.getMensajeExito().observe(getViewLifecycleOwner(),
                mensaje -> Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_SHORT).show());

        viewModel.getEventoAbrirMapa().observe(getViewLifecycleOwner(), event -> {
            String direccion = event.getContentIfNotHandled();
            Bundle args = new Bundle();
            args.putString("direccionCliente", direccion);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_trackingCadeteFragment_to_mapaEntregaFragment, args);
        });

        viewModel.getEventoLlamarTelefono().observe(getViewLifecycleOwner(), event -> {
            String telefono = event.getContentIfNotHandled();
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono));
            startActivity(dialIntent);
        });

        viewModel.getEventoNavegarEntregas().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
