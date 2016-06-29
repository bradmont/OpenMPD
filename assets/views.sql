-- views :


-- months:
drop view if exists month;

create view MONTHS as select distinct month from gift;


-- partner_giving_status

drop view if exists partner_giving_status;

create view if not exists partner_giving_status as
    select contact_id, tnt_people_id, giving_amount, type 
        from contact join contact_status 
            on contact_id=contact._id ;


--  partner_giving_by_month:
--  the total amount a partner has given each month
drop view if exists partner_giving_by_month;

create view if not exists partner_giving_by_month as
    select contact_id, month, sum(amount) as total_gifts 
        from gift 
        group by contact_id,month;



--  _monthly_base_giving:
-- all monthly gifts where a partner's giving equals their giving_amount

drop view if exists _monthly_base_giving;

create view if not exists _monthly_base_giving as 
    select month, sum(total_gifts) as base_total
    from partner_giving_status join partner_giving_by_month
        on partner_giving_status.contact_id = partner_giving_by_month.contact_id
        where total_gifts<=giving_amount and type='monthly'
        group by month
        order by month;


-- _monthly_base_giving_in_special_month
-- base giving in months where a partner gave extra
drop view if exists _monthly_base_giving_in_special_month;
create view if not exists _monthly_base_giving_in_special_month as
    select month, sum(giving_amount) as base_total 
    from  
         partner_giving_status A  
        join  
        partner_giving_by_month B  
        on A.contact_id = B.contact_id  
    where total_gifts>giving_amount  
    group by month ;


-- monthly_base_giving
-- month by month summary of monthly gifts
drop view if exists monthly_base_giving;
create view monthly_base_giving as 
 select month, sum(base_total) base_giving from 
    (select * from _monthly_base_giving union select * from _monthly_base_giving_in_special_month) 
    group by month;




-- regular_by_month (non-monthly regular gifts)
-- all regular gifts where a partner's giving equals their giving_amount
drop view if exists regular_by_month;
create view if not exists regular_by_month as
    select month, sum(total_gifts) as regular_giving
            from 
            partner_giving_status A join partner_giving_by_month B 
            on A.contact_id = B.contact_id 
        where total_gifts=giving_amount 
            and (type='regular' or type='annual')  
        group by month;



-- frequent_by_month (Frequent but irregular gifts)
-- all gifts from 'frequent' partners
drop view if exists frequent_by_month;
create view if not exists frequent_by_month as
    select month, sum(total_gifts) as frequent_giving
            from 
            partner_giving_status A join partner_giving_by_month B 
            on A.contact_id = B.contact_id 
        where type='frequent' group by month;


-- special_gifts_by_month
-- giving above GIVING_AMOUNT for all partners, except FREQUENT,
-- where giving_amount is an average
drop view if exists special_gifts_by_month;
create view if not exists special_gifts_by_month as 
    select month, sum(total_gifts) - sum(giving_amount) as special_gifts 
        from  
            (select * from  partner_giving_status A  
            join   partner_giving_by_month B
            on A.contact_id = B.contact_id  
        where total_gifts>giving_amount and type != 'frequent')  
        group by month ;
