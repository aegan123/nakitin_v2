document.addEventListener('DOMContentLoaded', () => {
    const $navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0);
    $navbarBurgers.forEach( el => {
        el.addEventListener('click', () => {
            const target = el.dataset.target;
            const $target = document.getElementById(target);
            el.classList.toggle('is-active');
            $target.classList.toggle('is-active');
        });
    });
});

function checkPassword(element) {
    const otherPasswordElement = element.id === "password" ? document.getElementById("confirmPassword") : document.getElementById("password")
    if (element.value === otherPasswordElement.value) {
        element.classList.remove("is-danger")
        otherPasswordElement.classList.remove("is-danger")
    } else if (element.value < otherPasswordElement.value) {
        element.classList.add("is-danger")
    } else {
        otherPasswordElement.classList.add("is-danger")
    }
}

function toggleDropdown(event) {
    event.stopPropagation();
    const element = document.getElementById("dropdown")
    element.classList.toggle("is-active")
}
