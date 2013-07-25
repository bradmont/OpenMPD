-- views:

-- month
create view if not exists month as select distinct month from gift order by month desc; 

-- partner_giving_status:

create view if not exists partner_giving_status as
    select contact_id, tnt_people_id, giving_amount, partner_type 
        from contact join contact_status 
            on contact_id=contact._id 
            where tnt_people_id not like '-%';

--  partner_giving_by_month:
create view if not exists partner_giving_by_month as
    select tnt_people_id, month, sum(amount) as total_gifts 
        from gift group by tnt_people_id,month;

--  _monthly_base_giving:
-- all monthly gifts where a partner's giving equals their giving_amount

create view if not exists _monthly_base_giving as 
    select month, sum(total_gifts) as base_total
    from partner_giving_status join partner_giving_by_month
        on partner_giving_status.tnt_people_id = partner_giving_by_month.tnt_people_id
        where total_gifts=giving_amount and partner_type=6
        group by month
        order by month;
-- _monthly_base_giving_in_special_month
-- base giving in months where a partner gave extra
create view if not exists _monthly_base_giving_in_special_month as
    select month, sum(giving_amount) as base_total 
    from  
         partner_giving_status A  
        join  
        partner_giving_by_month B  
        on A.tnt_people_id = B.tnt_people_id  
    where total_gifts>giving_amount  
    group by month ;

-- monthly_base_giving
create view monthly_base_giving as 
    select a.month, a.base_total+b.base_total base_giving 
        from _monthly_base_giving a join _monthly_base_giving_in_special_month b 
       on a.month = b.month;


-- regular_by_month (non-monthly regular gifts)
-- all regular gifts where a partner's giving equals their giving_amount
create view if not exists regular_by_month as
    select month, sum(total_gifts) as regular_giving
            from 
            partner_giving_status A join partner_giving_by_month B 
            on A.tnt_people_id = B.tnt_people_id 
        where total_gifts=giving_amount and (partner_type=5 or partner_type=4)  group by month;



-- special_gifts_by_month
create view if not exists special_gifts_by_month as 
    select month, sum(total_gifts) - sum(giving_amount) as special_gifts 
        from  
            (select * from  partner_giving_status A  
            join   partner_giving_by_month B
            on A.tnt_people_id = B.tnt_people_id  
        where total_gifts>giving_amount)  
        group by month ;
