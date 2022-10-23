package main

import (
	"fmt"
	"math/rand"
	"sync"
)

var (
	wg     sync.WaitGroup
	n      = 3
	arrLen = 5
)

func genModifier() int {
	b := rand.Intn(2)
	if b == 0 {
		return -1
	} else {
		return 1
	}
}

func modify(array []int) {
	i := rand.Intn(len(array))
	array[i] = genModifier()
	wg.Done()
}

func sum(a []int) int {
	r := 0
	for _, v := range a {
		r += v
	}
	return r
}

func genArray() []int {
	var a []int
	for i := 0; i < arrLen; i += 1 {
		a = append(a, rand.Intn(10))
	}
	return a
}

func main() {
	var arrays [][]int
	for i := 0; i < n; i += 1 {
		arrays = append(arrays, genArray())
	}

	for {
		s0, s1, s2 := sum(arrays[0]), sum(arrays[1]), sum(arrays[2])
		if s0 == s1 && s0 == s2 {
			break
		}

		wg.Add(n)
		for i := 0; i < n; i += 1 {
			go modify(arrays[i])
		}
		wg.Wait()
	}

	for i := 0; i < n; i += 1 {
		fmt.Printf("%v\n", arrays[i])
	}
}
