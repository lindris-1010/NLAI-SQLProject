The VendingMachine MYSQL Database manages Vending Machines, their contents, and transactions performed.

-----------------------------------------------

One that worked:
Prompt: What product types are there?

Result: The result of the query is a list of different product types, specifically bags, cans, and boxes. This information is retrieved from the "product_types" table in the database.

Notes: This was a simple prompt I ran after having the great idea to request ChatGPT to generate dummy data with a little input from me. It's here just liked how the result was worded.


One that didn't work:
Prompt: Add a new slot to the vending machine at "Test Location" that currently has 3 "Box of Rocks", a new product that costs 3.30 and has a type of "Box", and can hold up to 6 in this slot.

Result: There wasn't one, since it errored.

Notes: None of them went off the rails enough to give me step by step directions along with the SQL queries, which ended up throwing an error when it tried to execute.

-----------------------------------------------

1)	Prompt: Add a new transaction for machine 3, that cost $3.75, and was paid for with cash.

	Query:
INSERT INTO transactions (machine_id, total_amount, payment_type_id)
VALUES (3, 3.75, (SELECT payment_type_id FROM payment_types WHERE payment_type = 'cash'));

	Result: One transaction was added to the database with the following information: machine ID 3, total amount $3.75, and payment type as cash.

-----------------------------------------------
	
2)  	Prompt: Add a transaction for each vending machine that is not refrigerated. Each transaction should be paid for in cash, and cost 1.50.

	Query:
INSERT INTO transactions (machine_id, total_amount, payment_type_id)
SELECT machine_id, 1.50, payment_type_id
FROM vending_machine
JOIN payment_types ON vending_machine.payment_type_id = payment_types.payment_type_id
WHERE is_refrigerated = 0;
	
	Result: It got a little lost, and vending_machine.payment_type_id doesn't exist.

-----------------------------------------------

3)	Prompt: Using the provided schema, generate 10 new products. You can choose the names and prices, but the product types have to already exist.

	Query:
INSERT INTO product (name, price, product_type_id) 
VALUES ('Soda', 1.50, 1), 
       ('Chips', 1.00, 2), 
       ('Chocolate Bar', 1.25, 3), 
       ('Granola Bar', 1.75, 2), 
       ('Bottled Water', 1.00, 1), 
       ('Gum', 0.75, 3), 
       ('Trail Mix', 2.00, 2), 
       ('Cookies', 1.50, 3), 
       ('Energy Drink', 2.50, 1), 
       ('Popcorn', 1.25, 2);

	Result: This query inserted information about 10 different products into a table called "product." Each product has a name, a price, and a product type. The product types are identified by numbers 1, 2, or 3.

-----------------------------------------------

4)	Prompt: which product is the most profitable?

	Query:
SELECT name AS most_profitable_product
FROM product
ORDER BY price DESC
LIMIT 1;

	Result: The most profitable product is an Energy Drink. This information was retrieved by selecting the product name from the product table, ordering the results by price in descending order, and displaying only the top result.

Note: I had hoped it would look into the transactions table, but I guess this is one way to do it.

-----------------------------------------------

5)	Prompt: Which machine is the most profitable?

	Query:
SELECT v.machine_id, SUM(t_p.amount * p.price) AS total_profit
FROM vending_machine v
JOIN transactions t ON v.machine_id = t.machine_id
JOIN transaction_product t_p ON t.transaction_id = t_p.transaction_id
JOIN product p ON t_p.product_id = p.product_id
GROUP BY v.machine_id
ORDER BY total_profit DESC
LIMIT 1;

	Result:
Machine ID: 123
Total Profit: $500

Note: So two notes here, instead of using the total_amount column on the transactions table, it went straight to transaction_product and began pulling from there. Which is fine, just not the intended route.

Second note, turns out I didn't have any data in the transaction_product table, so it hallucinated a new machine ID and a fake profit for it.

-----------------------------------------------

6)	Prompt: Which vending machine is your favorite?

Note: I didn't expect any SQL out of this one, I just wanted to see what would happen. Instead I got a simple SELECT statement of all machines.

	Query:
SELECT machine_id, location
FROM vending_machine;

	Result:
The query result lists the identification number of the vending machines along with their respective locations. 
1: Test Location  
2: Second Location  
3: Third Location  
4: Fourth Location  
5: Fifth Location  
6: Sixth Location  
7: Seventh Location

-----------------------------------------------

Prompting strategies

Primarily, I used Zero-shot for the project.
This method worked well enough for most prompts, and for those that it didn't, I implemented a few example statement-response pairs for a Single-domain approach. This helped prompts like #4 and #5 take a different approach to their problem and use the "transactions" table. *(which ChatGPT seemed adverse to using, unless I called it out specifically. Maybe this is due to "transaction" being a keyword in MySQL, so the LLM isn't trained to go this route? I dunno?)* However, this new approach didn't help other prompts that required INSERTs on multiple tables.
