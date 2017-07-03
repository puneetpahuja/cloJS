const a = 10;
const b = 'hello';
const c = null;
const d = a;
const e = 1 + 2 + 3 + 4;
const f = {
    'a': 1,
    'b': 2,
    'c': 3
};
const g = [
    1,
    2,
    3
];
const h = g[2];
console.log(g[1]);
let a = 2, b = 5;
if (1)
    2 + 2;
else if (3)
    4;
else if (5 + 5)
    6;
else if (7)
    8;
x => {
    if (is_array_member(form)) {
        get_array_member(form);
        return 1 + 2;
    } else if (is_defn(form))
        return get_defn(form);
    else if (is_def(form))
        return get_const(form);
    else if (is_if(form))
        return get_if(form);
    else if (is_do(form))
        return get_do(form);
    else if (is_vec(form))
        return get_vec(form);
    else if (is_lambda(form))
        return get_lambda(form);
    else if (is_map_ds(form))
        return get_map_ds(form);
    else if (is_literal(form))
        return get_literal(form);
    else if (is_operator(form))
        return get_operator(form);
    else if (is_fn_call(form))
        return get_fn_call(form);
    else if (true)
        return null;
};
x => {
    console.log(x);
    let a = 2, b = 5;
    return console.log(5 + x + a + b);
};
const print_multiple = (x, y, z, a, b, c) => {
    if (1 === 1) {
        console.log('x :', x);
        console.log('y :', y);
        return z;
    } else {
        console.log('a :', a);
        console.log('b :', b);
        return console.log('c :', c);
    }
};
const z = print_multiple(1 + 2 + 3 + 4 + 5 + 6, 'a', true, [
    1,
    2,
    3,
    {
        'a': 1,
        'b': c,
        'c': [
            1,
            2,
            {
                'x': 10,
                'y': [
                    20,
                    30
                ]
            },
            3
        ]
    }
], null, {
    'a': 1,
    'b': [
        1,
        2,
        3,
        {
            'a': 1,
            'b': c,
            'c': [
                1,
                2,
                {
                    'x': 10,
                    'y': [
                        20,
                        30
                    ]
                },
                3
            ]
        }
    ]
});
const factorial = n => {
    if (n === 0)
        return 1;
    else
        return n * factorial(n - 1);
};
console.log('factorial of', a, 'is', factorial(a));
const a = () => {
    return 5;
};
console.log(a());
readline();
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
mff(mvv, test);
