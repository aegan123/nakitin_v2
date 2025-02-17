function fetchCsv(eventId) {
    fetch("/event/" + eventId + "/export", {
        method: "GET",
    }).then(async res => {
        if (res.ok) console.log("Status: "+res.status)
        if (res.status >= 400 && res.status <= 499) {
            console.log("Status: "+res.status)
        }
        if (res.status >= 500) {
            console.log("Status: "+res.status)
        }
    })
}
