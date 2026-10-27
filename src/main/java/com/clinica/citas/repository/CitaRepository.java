package com.clinica.citas.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import com.clinica.citas.model.Cita;
import com.clinica.citas.model.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    /** Citas de un médico, en los estados indicados, que inician dentro del rango [desde, hasta). */
    @Query("""
            select c from Cita c
            where c.medico.idPersona = :idMedico
              and c.estado in :estados
              and c.fechaHoraInicio >= :desde
              and c.fechaHoraInicio < :hasta
            """)
    List<Cita> buscarCitasDelMedico(@Param("idMedico") Long idMedico,
                                    @Param("estados") Collection<EstadoCita> estados,
                                    @Param("desde") LocalDateTime desde,
                                    @Param("hasta") LocalDateTime hasta);
}
