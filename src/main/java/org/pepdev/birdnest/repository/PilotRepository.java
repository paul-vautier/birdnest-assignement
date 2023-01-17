package org.pepdev.birdnest.repository;

import org.pepdev.birdnest.model.PilotInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PilotRepository extends CrudRepository<PilotInfo, String> {
}
