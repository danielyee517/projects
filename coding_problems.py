import math

# Converts a float string into a floating point number (e.g. "34.575" -> 34.575)
def convert_to_float(float_string):
    result = 0
    # Finds the index of the decimal point
    period_index = float_string.find('.')
    # If there is no decimal, then simply add digit * 10 ^ power of the place value to the result
    if period_index == -1:
        for i in range(len(float_string)):
            result += int(float_string[i]) * math.pow(10, len(float_string) - (i+1))
    else:
        # If there is a decimal point, then add digit * 10 ^ power of the place value depending on how far it is relative to the decimal
        for i in range(len(float_string)):
            if i < period_index:
                result += int(float_string[i]) * math.pow(10, len(float_string) - (len(float_string) - period_index) - i - 1)
            elif float_string[i] != '.':
                result += int(float_string[i]) * math.pow(10, period_index - i)
    return result

# Finds all duplicate elements in the array and returns the duplicate element with the # of times that they appear
def duplicates(arr):
    occurences = {}
    # Increments # of times you've seen this element in the array
    for elem in arr:
        if elem in occurences.keys():
            occurences[elem] += 1
        else:
            occurences[elem] = 1
    # Removes all non-duplicates (the value for that key is 1)
    for k,v in occurences.items():
        if v == '1':
           del occurences[k]
    return occurences

# Finds the xor of a and b without using the xor function
def a_xor_b(a, b):
    # Turns a/b from ints into binary strings
    a_bin = '{0:08b}'.format(a)
    b_bin = '{0:08b}'.format(b)
    if len(a_bin) > len(b_bin):
        return xor_helper(a_bin, b_bin)
    else:
        return xor_helper(b_bin, a_bin)

# used so that a can be the longer binary string in a_xor_b
def xor_helper(a, b):
    result = 0
    # Loops through all of the digits of the longer binary string from right to left
    for i in range(len(a)):
        # Checks to see if current index is different in both strings (e.g. 1/0 or 0/1) and adds the corresponding value
        if i >= len(b):
            if a[-1 - i] == '1':
                result += math.pow(2,i)
        elif b[-1 - i] != a[-1 -i]:
            result += math.pow(2, i)
    return int(result)


saved_multiplications = []
# Returns a to the bth power without using the math.pow function and optimizes to take half the # of multiplications
def a_exp_b(a, b):
    global saved_multiplications
    # Take advantage of the memoization by using the smaller number as the base
    if a > b:
        saved_multiplications = [None] * int(math.sqrt(a) + 1)
        return exp_helper(a,b)
    else:
        saved_multiplications = [None] * int(math.sqrt(b) + 1)
        return exp_helper(b,a)

def exp_helper(a, b):
    global saved_multiplications
     # The base case is if you reach a * 1 which is a
    if b == 1:
        return a
    # If you have already done this exponent level (e.g. 4 ^ 6, then just return the saved value for 4 ^ 6, which is saved_multiplications[6])
    elif b < len(saved_multiplications):
        if saved_multiplications[b] != None:
            return saved_multiplications[b]
        else:
            if b % 2 == 0:
                saved_multiplications[b] = exp_helper(a,b/2) * exp_helper(a, b/2)
                return saved_multiplications[b]
            else:
                saved_multiplications[b] = exp_helper(a,b/2) * exp_helper(a, b/2 + 1)
                return saved_multiplications[b]
    # Make the recursive call if you have not reached the base case
    else:
        if b % 2 == 0:
            return exp_helper(a,b/2) * exp_helper(a, b/2)
        else:
            return exp_helper(a,b/2) * exp_helper(a, b/2 + 1)


