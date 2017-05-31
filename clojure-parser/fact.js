var a = 10;
var b = "hello";
var c = null;
var d = a;

var e = 1 + 2 + 3 + 4;

function factorial (n)
{
  if (n === 0)
  {
    return 1;
  }
  else
  {
    return n * factorial (n - 1);            
  }
}

factorial (a);

def f = [1, 2, e, ["a", null, true], false];
def g = {1:2, "a":b}
