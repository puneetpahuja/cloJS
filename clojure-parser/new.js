const a = [
    1,
    2,
    3,
    4
];
const b = a[2];
console.log(a[3]);
if (array-member?(form))
    get-array-member(form);
else if (defn?(form))
    get-defn(form);
else if (def?(form))
    get-const(form);
else if (if?(form))
    get-if(form);
else if (do?(form))
    get-do(form);
else if (vec?(form))
    get-vec(form);
else if (lambda?(form))
    get-lambda(form);
else if (map-ds?(form))
    get-map-ds(form);
else if (literal?(form))
    get-literal(form);
else if (operator?(form))
    get-operator(form);
else if (fn-call?(form))
    get-fn-call(form);
else if (true)
    null;