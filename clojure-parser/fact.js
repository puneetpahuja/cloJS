var a = 6;
function factorial(n) {
    if (n === 0)
        return 1;
    else
        return n * factorial(n - 1);
}
console.log('factorial of', a, 'is', factorial(a));