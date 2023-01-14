package org.pepdev.birdnest.dao;

import org.pepdev.birdnest.model.PilotInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DroneRepository extends CrudRepository<PilotInfo, String> {
}
