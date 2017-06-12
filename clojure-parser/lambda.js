function a(x) {
    console.log(x);
    return console.log(1);
}
var b = function (x) {
    console.log(x * x);
    return console.log(2);
};
a(5);
b(6);