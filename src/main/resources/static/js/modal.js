document.addEventListener('keydown', (event) => {
    if(event.key === "Escape") {
        closeAllModals()
    }
})

document.addEventListener('DOMContentLoaded', () => {
    (document.querySelectorAll('.modal-background, .modal-close, .modal-card-head .delete') || []).forEach(($close) => {
        const $target = $close.closest('.modal');

        $close.addEventListener('click', () => {
            closeModal($target);
        });
    });
})

function closeAllModals() {
    (document.querySelectorAll('.modal') || []).forEach((modal) => {
        modal.classList.remove('is-active');
    }
    )
}

function displayModal(element) {
    document.getElementById(element.id + "_modal").classList.add("is-active")
}

function closeModal(element) {
    element.closest(".modal").classList.remove("is-active")
}
