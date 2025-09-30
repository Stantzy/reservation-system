package io.github.stantzy.reservation_system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/* класс обработки HTTP-запросов */

@RestController
public class ReservationController {
    /* логгирование */
    private static final Logger log =
            LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public Reservation getReservationById(
            @PathVariable("id")Long id
    ) {
        log.info("Called getReservationById: id=" + id);
        return reservationService.getReservationById(id);
    }

    @GetMapping
    public List<Reservation> getAllReservations() {
        log.info("Called getAllReservations");
        return reservationService.findAllReservations();
    }
}
