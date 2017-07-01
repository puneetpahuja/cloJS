const commaParser = input => input.startsWith(',') ? [null, input.slice(1)] : null
const spaceParser = input => input.match(/^[\s\n]/) ? [null, input.slice(input.match(/\S/).index)] : null
const nullParser = input => input.startsWith('null') ? [null, input.slice(4)] : null
const colonParser = input => input.startsWith(':') ? [null, input.slice(1)] : null
const boolParser = input => input.startsWith('true') ? [true, input.slice(4)] : (input.startsWith('false') ? [false, input.slice(5)] : null)
const numberParser = (input, num = /^[-+]?(\d+(\.\d*)?|\.\d+)([e][+-]?\d+)?/i, x) => (x = input.match(num)) ? [parseFloat(x[0]), input.slice(x[0].length)] : null

const stringParser = (input, i = 1) => {
  if (!input.startsWith('"')) return null
  while (input[i] !== '"') (input[i] === '\\') ? i = i + 2 : i++
  return [input.substring(1, i), input.slice(i + 1)]
}

const arrayParser = (input) => {
  if (!input.startsWith('[')) return null
  input = input.slice(1)
  let arr = []
  while (true) {
    let space
    input = (space = spaceParser(input)) ? space[1] : input
    input = valueParser(input)
    if (!input) { return null } arr.push(input[0])
    input = (space = spaceParser(input[1])) ? space[1] : input[1]
    let temp = commaParser(input)
    if (!temp) { break } input = temp[1]
  }
  return (input.startsWith(']')) ? [arr, input.slice(1)] : null
}

const objectParser = (input) => {
  if (!input.startsWith('{')) return null
  let obj = {}
  input = input.slice(1)
  while (true) {
    let space
    input = (space = spaceParser(input)) ? space[1] : input
    input = stringParser(input)
    if (!input) { return null } let [key, value] = input
    value = (space = spaceParser(value)) ? space[1] : value
    value = colonParser(value)
    if (!value) { return null }
    value = (space = spaceParser(value[1])) ? space[1] : value
    value = valueParser(value)
    if (!value) { return null } obj[key] = value[0]
    input = (space = spaceParser(value[1])) ? space[1] : value[1]
    let temp = commaParser(input)
    if (!temp) { break } input = temp[1]
  }
  return (input.startsWith('}')) ? [obj, input.slice(1)] : null
}

const anyOneParserFactory = (...parsers) => (input) => parsers.reduce((accum, parser) => (accum === null) ? parser(input) : accum, null)
const valueParser = anyOneParserFactory(nullParser, boolParser, numberParser, stringParser, objectParser, arrayParser)
const inpStr = require('fs').readFileSync('example.json').toString()
const output = valueParser(inpStr)
output ? console.log(JSON.stringify(output[0], null, 2)) : console.log('Invalid JSON')
