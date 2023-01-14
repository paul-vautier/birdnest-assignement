'use strict';
const fetchConfig = {
    method: 'GET',
    headers: {
        'Accept': 'application/json',

    },
};

/**
 * Retrieve the data of the pilots that entered the No-Fly-Zone
 */
function retrievePilotData() {
    fetch('/drones', fetchConfig)
        .then(response => response.json())
        .then(response =>updateDroneData(response));
}

/**
 * Adds the drone data to the list
 * @param {{serialNumber : string, pilotId : string, firstName : string, lastName : string,  latestObservation : string, phoneNumber : string, phoneNumber : string, createdDt : string, email : string, closestDistance : number, }[]} pilotList
 */
function updateDroneData(pilotList) {
    let tbody = document.querySelector('#drones-table tbody');
    while (tbody.children.length > 0) {
        tbody.removeChild(tbody.children.item(tbody.children.length - 1));
    }

    pilotList.forEach((drone, index)=> {
        tbody.innerHTML+= `<th scope="row"> ${index + 1}</td>
            <td>${drone.lastName} ${drone.firstName}</td>
            <td>${drone.closestDistance}</td>
            <td>${drone.latestObservation}</td>
            <td>${drone.phoneNumber}</td>
            <td><a href="mailto:${drone.email}">${drone.email}</a></td>
        `
    })
}

setInterval(retrievePilotData, 2000);