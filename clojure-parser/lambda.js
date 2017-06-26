const a = x => {
    console.log(x);
    return console.log(1);
};
const b = x => {
    console.log(x * x);
    return console.log(2);
};
const a = 5, b = 6, c = 1 + 2, d = fun(5, 6, 1 + 2), e = x => {
        console.log(x * x);
        return console.log(2);
    };
let a = 2, b = 5;
a(5);
b(6);