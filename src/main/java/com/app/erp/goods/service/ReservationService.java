package com.app.erp.goods.service;

import com.app.erp.dto.reservation.ReservationDTO;
import com.app.erp.entity.reservation.Reservation;
import com.app.erp.goods.exceptions.ResourceNotFoundException;
import com.app.erp.goods.repository.ReservationRepository;
import com.app.erp.sales.service.OrderService;
import com.app.erp.user.service.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;


    @Autowired
    public ReservationService(
            ReservationRepository reservationRepository,
            ModelMapper modelMapper,
            NotificationService notificationService
    ) {
        this.reservationRepository = reservationRepository;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
    }

    public Page<ReservationDTO> getAllReservations(int page, int size) {
        Page<Reservation> reservations = reservationRepository.findAll(
                PageRequest.of(page, size)
        );
        return reservations.map(this::convertToDTO);
    }

    public ReservationDTO getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        return convertToDTO(reservation);
    }

    @Transactional
    public ReservationDTO updateReservation(Long id, ReservationDTO reservationDTO) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        reservation.setQuantity(reservationDTO.getQuantity());
        reservation.setStatus(reservationDTO.getStatus());

        Reservation updatedReservation = reservationRepository.save(reservation);
        return convertToDTO(updatedReservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        reservationRepository.deleteById(id);

        notificationService.createAndSendNotification(
                "RESERVATION_CANCELLED",
                "Cancelled reservation with ID: " + id,
                List.of("SALES_MANAGER", "INVENTORY_MANAGER")
        );
    }

    private ReservationDTO convertToDTO(Reservation reservation) {
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);
        dto.setProductId(reservation.getProduct().getId());
        dto.setProductName(reservation.getProduct().getProductName());
        dto.setOrderId(reservation.getOrder().getId());
        return dto;
    }
}