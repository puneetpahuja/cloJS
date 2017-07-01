const a = 6;
const factorial = n => {
    if (n === 0)
        return 1;
    else
        return n * factorial(n - 1);
};
console.log('factorial of', a, 'is', factorial(a));
