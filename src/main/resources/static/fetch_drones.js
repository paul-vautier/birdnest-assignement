'use strict';

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
        if (drone == null) {
            return;
        }
        tbody.innerHTML+= `<th scope="row"> ${index + 1}</td>
            <td>${drone.lastName} ${drone.firstName}</td>
            <td>${drone.closestDistance}</td>
            <td>${drone.latestObservation}</td>
            <td>${drone.phoneNumber}</td>
            <td><a href="mailto:${drone.email}">${drone.email}</a></td>
        `
    })
}

let socket = new SockJS('/gs-guide-websocket');
let stompClient = Stomp.over(socket);
stompClient.connect({}, function () {
    stompClient.subscribe('/drones', function (message) {
        updateDroneData(JSON.parse(message.body));
    });
});