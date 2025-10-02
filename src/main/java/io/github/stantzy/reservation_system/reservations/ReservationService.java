package io.github.stantzy.reservation_system.reservations;

/* класс обработки бизнес-логики */

import io.github.stantzy.reservation_system.reservations.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReservationService {
    private static final Logger log =
            LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository repository;
    private final ReservationMapper mapper;
    private final ReservationAvailabilityService availabilityService;

    public ReservationService(
            ReservationRepository repository,
            ReservationMapper mapper,
            ReservationAvailabilityService availabilityService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.availabilityService = availabilityService;
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity =
                repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found reservation by id = " + id)
                );

        return mapper.toDomainReservation(reservationEntity);
    }

    public List<Reservation> searchAllByFilter(
            ReservationSearchFilter filter
    ) {
        int pageSize = filter.pageSize() != null
                ? filter.pageSize() : 10; // hardcoded value
        int pageNumber = filter.pageNumber() != null
                ? filter.pageNumber() : 0; // hardcoded value

        var pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);

        List<ReservationEntity> allEntities = repository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );

        return allEntities.stream()
                .map(mapper::toDomainReservation)
                .toList();
    }

    public Reservation createReservation(
            @Valid Reservation reservationToCreate
    ) {
        if(reservationToCreate.status() != null)
            throw new IllegalArgumentException("Status should be empty");

        if(!reservationToCreate.endDate()
                .isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException(
                    "Start date must be 1 day earlier than end date"
            );
        }

        var entityToSave = mapper.toReservationEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);

        var savedEntity = repository.save(entityToSave);

        return mapper.toDomainReservation(savedEntity);
    }

    public Reservation updateReservation(
            Long id,
            @Valid Reservation reservationToUpdate
    ) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found reservation by id = " + id
                ));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot modify reservation: status=" +
                            reservationEntity.getStatus()
            );
        }
        if(!reservationToUpdate.endDate()
                .isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException(
                    "Start date must be 1 day earlier than end date"
            );
        }

        var reservationToSave = mapper.toReservationEntity(reservationToUpdate);
        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setStatus(reservationEntity.getStatus());

        var updatedReservation = repository.save(reservationToSave);

        return mapper.toDomainReservation(updatedReservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        var reservation = repository.findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException(
                                        "Not found reservation by id = " + id)
                        );
        if(reservation.getStatus() == ReservationStatus.APPROVED) {
            throw new IllegalStateException(
                    "Cannot cancel the reservation. Contact with manager"
            );
        }
        if(reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot cancel approved the reservation. " +
                    "Reservation was already cancelled"
            );
        }

        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
    }

    public Reservation approveReservation(Long id) {
        if(!repository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Not found reservation by id = " + id
            );
        }
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException
                        ("Not found reservation by id = " + id)
                );

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot approve reservation: status=" +
                            reservationEntity.getStatus()
            );
        }

        var isAvailableToApprove = availabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );
        if(!isAvailableToApprove) {
            throw new IllegalStateException(
                    "Cannot approve reservation because of conflict"
            );
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return mapper.toDomainReservation(reservationEntity);
    }
}
