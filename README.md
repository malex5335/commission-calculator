# About this Project

## Background

A long time ago I was part of a Team that was tasked to create a Tool to
calculate and hand down commissions for brokers that were selling different
products.\
The Challenge was to create something that was easy to maintain and add on to.\
We built a colossal mess of code that was hard to understand and not at all
easy to add on.\
We ended up creating 16+ Microservices that were highly coupled, even though
we only had 4 Devs in total.\
To be fair we had to deliver Microservices and no one of us really knew how
to create them.\
After 2 years we were told by some Code-Testers, that our code was functional
at best and that we should add plenty of tests to refactor our code.\
At this time we had over 200k LOC and 50 Tests that took 6h to run.\
I was quiet young and naive at the time and didn't really understand how to
solve all those problems or how good code looked like.\
We had external support in form of a Software Consultant that helped us
writing phenomenal tests and refactoring our code.\
Sadly he decided to leave the project due to conflicts with some of our team
members.\
Thanks to him we had about 150 written tests that took only 10min to run.\
Still our code coverage was only around 30% and we had a lot of technical
debt.\
I've learned a lot about testing and writing maintainable code, also dipped my
toes into the world of Architecture and Microservices.

After over 3 years of development we sat together and discussed what we could
learn from this project and how we could improve our skills.\
My colleagues thought of microservices as the holy grail and perfect for this
or the coming projects.\
I on the other hand thought that microservices - if done right and using their
full potential - are a great tool to build complex systems.\
But I also thought that our projects - although they were complex at the end -
didn't need to be this complex and would be better off staying a
deployment-monolith.

To put my opinion into perspective, we almost ever deployed 6 to 8 if not all
of our Microservices at the same time.\
Information of each Service was stored into individual Databases but needed
to be accessed and added to by multiple Services in order to make use of it.\
That in of iit self increased the coupling between the Services that you want
to minimize in a Microservice Architecture.\
To connect each and every Service we used REST-Api-Endpoints that were hard
maintain and sometimes as complex as a new language.\
Coupled - pun intended - with the problem above, the response time between
each service was ridiculously high.\
And I might need to remind you: we had to calculate many, many commissions
every day and month.\
My Colleagues didn't agree with me, and we decided to part ways.

## The Idea

I wanted to show that you can solve this problem with a deployment-monolith.\
First I wanted to create different modules, based on the 16+ Microservices we
had.\
But the more time passed the more I realized that I could - and should - start
with a really simple solution and add on to it.\
My first goal was to think about the problem from scratch and keep all our
problems in mind.

- easy extendable and maintainable code
- new, flexible commission-calcuulations are to the upmost importance
- fast in calculation
- keep it as simple as possible
- UI only through REST-APIs (we had JSF-Components before)
- easy and meaningful test that everyone wants to add to
- test execution including integration tests should take less than 10min
- screw code coverage but I want 100% behavior coverage
- keep documentation to a minimum and use it to explain why instead of how

## The Solution

I decided to create a Kotlin-App, because I wanted to learn Kotlin and the
best way to learn something is to use it.\
I also started without any framework, because I don't use what I don't need.\
And at the start I have my tests to tell my I'm right or wrong.

### Calculation

**Old**:\
We had a very complex, all encompassing calculation class that had a
decision tree to determine weather a commission type was to be calculated,
for which broker and which product.\
The commission types passed a lot of information to the calculation class -
we basically created a new language that tried to inform the decision tree.
I've worked on the commission type part of the project and had my troubles
with it, even though I wasn't the one that came up with the idea, I was the
one that had to maintain it and understood it the most.

**New**:\
This was the most outrageous part of the project, and I was really thinking
on how to solve it smartly.\
What I came up with was to invert the decision tree and create a simple
calculation class that only looks up all configurations, ask them whether
to be calculated now and depending on that executes the calculation.\
That way we can easily add new commission types, brokers, products and
rules without having to change the calculation class.\
This also allows us to test certain days and times without having to add
complex logic change the execution date.\
The configurations therefore store all the complex logic and the calculation
itself.\
Sure we have some extravagant logic but putting it where it belongs and
segregate it from the rest of the code makes it a lot easier to understand.
