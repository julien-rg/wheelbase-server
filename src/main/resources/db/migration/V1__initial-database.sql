--
-- PostgreSQL database dump
--

-- \restrict VDtgi1s0JKPaPnfNWYFfCTikiLOTqYhnvPuW2Uiwx9e0vERDLfnydpwbf6qNfU2

-- Dumped from database version 17.7 (Debian 17.7-3.pgdg13+1)
-- Dumped by pg_dump version 18.0

-- Started on 2026-01-16 13:44:49

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 4 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: pg_database_owner
--

-- CREATE SCHEMA public;


-- ALTER SCHEMA public OWNER TO pg_database_owner;

--
-- TOC entry 3502 (class 0 OID 0)
-- Dependencies: 4
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pg_database_owner
--

-- COMMENT ON SCHEMA public IS 'standard public schema';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 217 (class 1259 OID 24854)
-- Name: comments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.comments (
    id uuid NOT NULL,
    content text NOT NULL,
    created_at timestamp(6) with time zone,
    author_id uuid NOT NULL,
    post_id uuid NOT NULL
);


ALTER TABLE public.comments OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 24861)
-- Name: follows; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.follows (
    followed_id uuid NOT NULL,
    follower_id uuid NOT NULL,
    created_at timestamp(6) with time zone
);


ALTER TABLE public.follows OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 24866)
-- Name: post_images; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.post_images (
    id uuid NOT NULL,
    image_url text NOT NULL,
    post_id uuid NOT NULL
);


ALTER TABLE public.post_images OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 24873)
-- Name: post_likes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.post_likes (
    post_id uuid NOT NULL,
    user_id uuid NOT NULL,
    created_at timestamp(6) with time zone
);


ALTER TABLE public.post_likes OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 24878)
-- Name: posts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.posts (
    id uuid NOT NULL,
    community character varying(255) NOT NULL,
    content text,
    created_at timestamp(6) with time zone,
    author_id uuid NOT NULL,
    vehicle_id uuid NOT NULL,
    CONSTRAINT posts_community_check CHECK (((community)::text = ANY ((ARRAY['CAR'::character varying, 'MOTORBIKE'::character varying])::text[])))
);


ALTER TABLE public.posts OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 24886)
-- Name: user_communities; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_communities (
    user_id uuid NOT NULL,
    community character varying(255),
    CONSTRAINT user_communities_community_check CHECK (((community)::text = ANY ((ARRAY['CAR'::character varying, 'MOTORBIKE'::character varying])::text[])))
);


ALTER TABLE public.user_communities OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 24890)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    account_type character varying(255) NOT NULL,
    avatar_url character varying(255),
    bio character varying(255),
    email character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    CONSTRAINT users_account_type_check CHECK (((account_type)::text = ANY ((ARRAY['PUBLIC'::character varying, 'FOLLOWERS_ONLY'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 24898)
-- Name: vehicle_brands; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vehicle_brands (
    id uuid NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.vehicle_brands OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 24903)
-- Name: vehicles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vehicles (
    id uuid NOT NULL,
    community character varying(255) NOT NULL,
    custom_brand character varying(255),
    description text,
    model character varying(255) NOT NULL,
    year integer,
    brand_id uuid,
    owner_id uuid NOT NULL,
    CONSTRAINT vehicles_community_check CHECK (((community)::text = ANY ((ARRAY['CAR'::character varying, 'MOTORBIKE'::character varying])::text[])))
);


ALTER TABLE public.vehicles OWNER TO postgres;

-- Completed on 2026-01-16 13:44:49

--
-- PostgreSQL database dump complete
--

-- \unrestrict VDtgi1s0JKPaPnfNWYFfCTikiLOTqYhnvPuW2Uiwx9e0vERDLfnydpwbf6qNfU2

