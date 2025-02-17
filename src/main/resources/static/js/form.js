function editTaskSubmission(e, element) {
    e.preventDefault();
    const form = element.closest('form')
    const taskId = form['taskId'].value
    const data = {
        'name': form['nameInEdit'].value,
        'date': form['dateInEdit'].value,
        'startTime': form['startTimeInEdit'].value,
        'endTime': form['endTimeInEdit'].value,
        'id': taskId,
        'eventId': form['eventIdInEdit'].value,
        'personCount': form['personCountInEdit'].value,
    }
    sendFetch("/event/task/edit", "PUT", data, taskId, form)
}

function addTaskSubmission(e, element) {
    e.preventDefault();
    const form = element.closest('form')
    const eventId = form['eventId'].value
    const data = {
        'name': form['nameInAdd'].value,
        'date': form['dateInAdd'].value,
        'startTime': form['startTimeInAdd'].value,
        'endTime': form['endTimeInAdd'].value,
        'eventId': eventId,
        'personCount': form['personCountInAdd'].value
    }
    sendFetch("/event/" + eventId + "/task/add", "POST", data, eventId, form)
}

function sendFetch(url, method, data, id, form) {
    fetch(url, {
        method: method,
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data),
    }).then(async res => {
        if (res.ok) {
            form.reset();
            form.style.display = 'none';
            document.getElementById(id + '_fail').style.visibility = "hidden"
            document.getElementById(id + '_fail').style.display = "none"
            document.getElementById(id + '_success').style.display = ""
        }
        if (res.status === 400) {
            const responseJson = await res.json()
            const dateError = responseJson['errors'].find((element => element.field === "date"))
            const nameError = responseJson['errors'].find((element => element.field === "name"))
            const startTimeError = responseJson['errors'].find((element => element.field === "startTime"))
            const endTimeError = responseJson['errors'].find((element => element.field === "endTime"))
            if (nameError) {
                document.getElementById(id + '_nameError').textContent = nameError['defaultMessage']
            } else {
                document.getElementById(id + '_nameError').textContent = ""
            }
            if (dateError) {
                document.getElementById(id + '_dateError').textContent = dateError['defaultMessage']
            } else {
                document.getElementById(id + '_dateError').textContent = ""
            }
            if (startTimeError) {
                document.getElementById(id + '_startTimeError').textContent = startTimeError['defaultMessage']
            } else {
                document.getElementById(id + '_startTimeError').textContent = ""
            }
            if (endTimeError) {
                document.getElementById(id + '_endTimeError').textContent = startTimeError['defaultMessage']
            } else {
                document.getElementById(id + '_endTimeError').textContent = ""
            }
            document.getElementById(id + '_fail').style.visibility = "hidden"
            document.getElementById(id + '_fail').style.display = "none"
        }
        if (res.status !== 400 || res.status !== 200) {
            document.getElementById(id + '_fail').style.display = ""
        }
        location.reload()
    })
}
